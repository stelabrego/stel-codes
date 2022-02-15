(ns codes.stel.dev-blog.generator
  (:require [stasis.core :as stasis]
            [codes.stel.dev-blog.views :as views]
            [codes.stel.dev-blog.util :as util]
            [clojure.java.io :as io]
            [babashka.fs :as fs]
            [taoensso.timbre :as log]
            [codes.stel.dev-blog.config :refer [get-config]]
            [markdown.core :refer [md-to-html-string]]))

(defn create-tag-indices
  ([] (create-tag-indices (get-config)))
  ([{:keys [articles] :as _config}]
  (let [tags (->> articles
                  (mapcat :tags)
                  (set))]
    (for [tag tags]
      (let [article-has-tag? (fn [article] (some #{tag} (:tags article)))]
        {:title (str "#" (name tag)),
         :id tag
         :category :index
         :uri (str "/tags/" (name tag) "/"),
         :indexed-articles (->>
                            articles
                            (filter article-has-tag?)
                            (map :id))})))))

(defn create-category-indices
  ([] (create-category-indices (get-config)))
  ([{:keys [articles] :as _config}]
  (let [categories (->> articles
                        (map :category)
                        (set))]
    (for [category categories]
      {:title (util/kebab-case->title-case category),
       :id category
       :category :index
       :uri (str "/" (name category) "/"),
       :indexed-articles (->>
                          articles
                          (filter #(= category (:category %)))
                          (map :id))}))))

(defn realize-articles
  "Adds :uri and :render-markdown keys to each article map in articles vector"
  [articles]
  (for [{:keys [category resource-path id] :as article} articles]
    (assoc article
           :uri (str "/" (name category) "/" (name id) "/")
           ;; I could render right here or I could just add a render function.
           ;; Having a render function makes it much easier to inspect the full
           ;; map output of this function in the repl
           :render-resource (fn render-markdown []
                              (if-let [resource (io/resource resource-path)]
                                ;; TODO add support for other types of resources
                                ;; based on filename suffix
                                (md-to-html-string (slurp resource))
                                (throw (ex-info "Missing resource" {:article article})))))))

(defn gen-id->info [{:keys [articles tag-indicies category-indicies] :as world}]
  (fn [id]
    (or ;; Get top level value if present
        (get world id)
        ;; Else look in articles and indicies for a match
        (as-> (concat articles tag-indicies category-indicies) $
          (filter #(= id (:id %)) $)
          (first $)
          (if $ (assoc $ :id->info (gen-id->info world))
            (throw (ex-info "Bad call to id->info" {:id id})))))))

(comment (gen-id->info {}))

(defn realize-world
  "Creates fully realized site datastructure with or without drafts."
  ([] (realize-world (get-config) true))
  ([include-drafts?] (realize-world (get-config) include-drafts?))
  ([config include-drafts?]
   ;; Remove drafts if necessary
   (let [config (if include-drafts?
                  config
                  (update config :articles #(remove :draft? %)))]
     (-> config
     ;; Realize the articles
     (update :articles realize-articles)
     ;; Add tag indicies
     (assoc :tag-indicies (create-tag-indices config))
     ;; Add category indicies
     (assoc :category-indicies (create-category-indices config))))))

(comment (realize-world)
         (-> (realize-world) :category-indicies first))

(defn generate-page-list
  ([] (generate-page-list (realize-world)))
  ([{:keys [articles tag-indicies category-indicies] :as world}]
   (->> [{:uri "/" :category :home} {:uri "/404/" :category :404}]
        (concat articles tag-indicies category-indicies)
        (map #(assoc % :id->info (gen-id->info world))))))

(comment (-> (generate-page-list) first)
         (-> (generate-page-list) first
             :id->info (apply [:general]))
         (-> (generate-page-list) first
             :id->info (apply [:clojure])
             :id->info (apply [:coding-projects])))

(defn generate-page-index
  ([] (generate-page-index (generate-page-list)))
  ([page-list]
   (into {}
    (map (fn [page] [(:uri page) (fn [_] (views/render page))]) page-list))))

(comment (generate-page-index)
         (doseq [[n f] (generate-page-index)]
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
  (export-site (-> (realize-world false) generate-page-list generate-page-index) "dist/prod"))

(comment (export-prod))

(defn export-dev []
  (export-site (generate-page-index) "dist/dev"))

(comment (export-dev))

(defn export
  ([] (export-prod) (export-dev))
  ([_] (export)))

