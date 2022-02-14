(ns codes.stel.dev-blog.generator
  (:require [stasis.core :as stasis]
            [codes.stel.dev-blog.views :as views]
            [codes.stel.dev-blog.util :as util]
            [clojure.java.io :as io]
            [me.raynes.fs :refer [copy-dir]]
            [taoensso.timbre :as timbre :refer [info]]
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

(defn realize-world
  "Creates fully realized site datastructure with or without drafts."
  ([] (realize-world (get-config) true))
  ([include-drafts?] (realize-world (get-config) include-drafts?))
  ([config include-drafts?]
   ;; I'm using as-> here because the draft removal step affects the indicies.
   ;; I could just redefine config in a let binding instead.
    (as-> config $
      ;; Remove drafts first if necessary
       (if include-drafts? $ (update $ :articles #(remove :draft? %)))
       ;; Realize the articles
       (update $ :articles realize-articles)
       ;; Add tag indicies
       (assoc $ :tag-indicies (create-tag-indices $))
       ;; Add category indicies
       (assoc $ :category-indicies (create-category-indices $)))))

(comment (realize-world))

(defn id->info [{:keys [articles tag-indicies category-indicies]} id]
  (->> (concat articles tag-indicies category-indicies)
       (filter #(= id (:id %)))
       (first)))

(comment
 (concat [1 2 3])
 (id->info {:articles [{:id :poop :uri "/poop"}]} :poop))

(defn generate-index
  ([] (generate-index (realize-world)))
  ([{:keys [articles tag-indicies category-indicies] :as world}]
   (->> [{:uri "/" :category :home} {:uri "/404/" :category :404}]
        (concat articles tag-indicies category-indicies)
        ;; Add id->info lookup function to every page
        (map #(assoc % :id->info (partial id->info world)))
        (map #(vector (:uri %) (fn [_] (views/render (assoc % :world world)))))
        (into {}))))

(comment (doseq [[n f] (generate-index)]
           (println "rendering" n)
           (println (f nil))))

(defn generic-export
  ([site-index target-dir]
   (let [assets (io/file (io/resource "public/assets"))]
     (info "Building site...")
     (fs)
     (stasis/empty-directory! target-dir)
     (stasis/export-pages site-index target-dir)
     (copy-dir assets target-dir)
     (info "Build successful")
     (info (str "Located in " target-dir)))))

(defn export-prod []
  (generic-export (generate-index (realize-world)) "/dist/dev"))

(defn export-dev []
  (generic-export (generate-index (realize-world)) "/dist/prod"))

(defn export
  ([] (export-prod) (export-dev))
  ([_] (export)))

(comment
  (export-dev))
