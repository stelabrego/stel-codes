(ns codes.stel.dev-blog.generator
  (:require [stasis.core :as stasis]
            [codes.stel.dev-blog.views :as views]
            [codes.stel.dev-blog.util :as util]
            [clojure.java.io :as io]
            [babashka.fs :as fs]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [codes.stel.dev-blog.config :refer [get-config]]
            [markdown.core :refer [md-to-html-string]]))

(defn create-tag-index
  ([] (create-tag-index (get-config)))
  ([config]
   (->> config
        ;; {:foo {:tags [:bar :baz]} :bee {:tags [:baz :boo]}}
        ;; Create a map shaped like tag -> [page-ids]
        (reduce-kv
         (fn [m id {:keys [tags]}]
           ;; merge-with is awesome!
           (if tags (merge-with into m (zipmap tags (repeat [id]))) m))
         {})
        ;; {:bar [:foo], :baz [:foo :bee], :boo [:bee]}
        ;; Then change the val into a map with more info
        (reduce-kv
         (fn [m tag ids]
           (assoc m [:tags tag] {:index ids :title (str "#" (name tag)) :uri (str "/tags/" (name tag) "/")}))
         {})
        ;; {:bar {:index [:foo], :title "#bar", :uri "/tags/bar/"},
        ;;  :baz {:index [:foo :bee], :title "#baz", :uri "/tags/baz/"},
        ;;  :boo {:index [:bee], :title "#boo", :uri "/tags/boo/"}}
        )))

(comment
 (create-tag-index)
 (create-tag-index {:foo {:tags [:bar :baz]} :moo {}})
 (create-tag-index {:foo {:tags [:bar :baz]} :bee {:tags [:baz :boo]}}))

(defn id->uri [id]
  {:pre [(vector? id)]}
  (str "/" (string/join "/" (map name id)) "/"))

(defn create-group-index
  ([] (create-group-index (get-config)))
  ([config]
   (->> config
        ;; {[:blog :foo] {:title "Foo"} [:projects :bee] {:title "Bee"}}
        ;; Then create a map shaped like group -> [page-ids]
        (reduce-kv
         (fn [m id _]
           (if (and (vector? id) (> (count id) 1))
             (merge-with into m {(vec (butlast id)) [id]}) m))
         {})
        ;; {:blog [[:blog :foo]], :projects [[:projects :bee]]}
        ;; Then change the val into a map with more info
        (reduce-kv
         (fn [m group-id ids]
           (assoc m group-id {:index ids :title (util/kebab-case->title-case (last group-id)) :uri (id->uri group-id)}))
         {}))))

(comment
 (= (create-group-index {[:blog :foo] {:title "Foo"} [:blog :archive :baz] {:title "Baz"} [:projects :bee] {:title "Bee"}})
    {[:blog]
     {:index [[:blog :foo]], :title "Blog", :uri "/blog/"},
     [:blog :archive]
     {:index [[:blog :archive :baz]],
      :title "Archive",
      :uri "/blog/archive/"},
     [:projects]
     {:index [[:projects :bee]], :title "Projects", :uri "/projects/"}})
 (create-group-index {:foo {:group :blog-posts} :bee {:group :coding-projects}}))

(defn realize-pages
  "Adds :uri and :render-resource keys to each article map in articles vector"
  ([] (realize-pages (get-config)))
  ([config]
   (reduce-kv
    (fn [m id {:keys [resource uri tags] :as v}]
      (if (vector? id)
        (assoc m id
               (merge v {;; Turn the basic tags vector into a vector of page ids
                         :tags (vec (map #(vector :tags %) tags))
                         :uri (or uri (id->uri id))
                         ;; TODO add support for other types of resources
                         ;; based on filename suffix
                         :render-resource (if resource
                                            (if-let [resource-file (io/resource resource)]
                                              (fn render-markdown []
                                                (md-to-html-string (slurp resource-file)))
                                              (throw (ex-info (str id ": Resource not found") {:id id})))
                                            (constantly nil))}))
        (assoc m id v)))
    {} config)))

(comment (realize-pages))

(defn gen-id->info [realized-config]
  (fn id->info [id]
    (if-let [entity (get realized-config id)]
        (assoc entity :id->info id->info)
        (throw (ex-info (str "id->info error: id " id " not found")
                        {:id id :realized-config realized-config})))))

(comment (gen-id->info {}))

;; Taken from https://clojuredocs.org/clojure.core/merge
(defn deep-merge [a & maps]
  (if (map? a)
    (apply merge-with deep-merge a maps)
    (apply merge-with deep-merge maps)))

(defn realize-config
  "Creates fully realized site datastructure with or without drafts."
  ([] (realize-config (get-config) true))
  ([config-or-include-drafts?] (if (map? config-or-include-drafts?)
                                 (realize-config config-or-include-drafts? true)
                                 (realize-config (get-config) config-or-include-drafts?)))
  ([config include-drafts?]
   {:pre [(map? config) (boolean? include-drafts?)]}
   ;; Allow users to define their own overrides via deep-merge
   (->> config
        (deep-merge {[] {:uri "/"} [:404] {:uri "/404/"}})
        (deep-merge (create-group-index config))
        (deep-merge (create-tag-index config))
        realize-pages)))

(comment (realize-config))

(defn generate-page-list
  ([] (generate-page-list (realize-config)))
  ([realized-config]
   (->> realized-config
        ;; If key is vector, then it is a page
        (reduce-kv (fn [page-list id v]
                     (if (vector? id)
                       (conj page-list (assoc v :id id))
                       page-list)) [])
        (map #(assoc % :id->info (gen-id->info realized-config))))))

(comment (-> (generate-page-list) first)
         (-> (generate-page-list) first
             :id->info (apply [:_meta]))
         (-> (generate-page-list) first
             :id->info (apply [[:blog-posts :using-directus-cms]]))
         (-> (generate-page-list) first
             :id->info (apply [[:clojure]])
             :id->info (apply [:coding-projects])))

(defn generate-site-index
  ([] (generate-site-index (generate-page-list)))
  ([page-list]
   (into {}
    (map (fn [page] [(:uri page) (fn [_] (views/render page))]) page-list))))

(comment (generate-site-index)
         (doseq [[n f] (generate-site-index)]
           (println "rendering" n)
           (println (f nil))))

(defn export-site
  ([site-index target-dir]
   (let [assets (io/file (io/resource "public/assets"))]
     (log/info "Building site...")
     (fs/create-dirs target-dir)
     (stasis/empty-directory! target-dir)
     (stasis/export-pages site-index target-dir)
     (fs/copy-tree assets (str target-dir "/assets"))
     (log/info "Build successful")
     (log/info (str "Located in " target-dir)))))

(defn export-prod []
  (export-site (-> (get-config) (realize-config false) (generate-page-list) (generate-site-index)) "dist/prod"))

(comment (export-prod))

(defn export-dev []
  (export-site (generate-site-index) "dist/dev"))

(comment (export-dev))

(defn export
  ([] (export-prod) (export-dev))
  ([_] (export)))

