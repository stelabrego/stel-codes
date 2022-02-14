(ns codes.stel.dev-blog.state
  (:require [clojure.java.io :as io]
            [codes.stel.dev-blog.config :refer [config get-config]]
            [camel-snake-kebab.core :as csk]
            [markdown.core :refer [md-to-html-string]]
            [clojure.string :as string]))

(defn kebab-case->title-case
  [s]
  (->> (string/split (name s) #"-")
       (map string/capitalize)
       (string/join " ")))

(comment
 (string/split (name :educational-media) #"-")
 (kebab-case->title-case :educational-media))

(defn kebab-case->lower-case
  [s]
  (->> (string/split (name s) #"-")
       (map string/lower-case)
       (string/join " ")))

(defn tag-name->index-uri [tag] (as-> tag $ (name $) (string/lower-case $) (csk/->kebab-case $) (str "/tags/" $ "/")))

(defn create-tag-indices
  ([] (create-tag-indices (config)))
  ([{:keys [articles] :as _config}]
  (let [tags (->> articles
                  (mapcat :tags)
                  (set))]
    (for [tag tags]
      (let [article-has-tag? (fn [article] (some #(= % tag) (:tags article)))]
        {:title (str "#" (name tag)),
         :id tag
         :category :index
         :uri (str "/tags/" (name tag) "/"),
         :indexed-articles (->>
                            articles
                            (filter article-has-tag?)
                            (map :id))})))))

(defn create-category-indices
  ([] (create-category-indices (config)))
  ([{:keys [articles] :as _config}]
  (let [categories (->> articles
                        (map :category)
                        (set))]
    (for [category categories]
      {:title (kebab-case->title-case category),
       :id category
       :category :index
       :uri (str "/" (name category) "/"),
       :indexed-articles (->>
                          articles
                          (filter #(= category (:category %)))
                          (map :id))}))))

(defn realize-articles
  "Adds :uri and :render-markdown kv pairs to each article map in articles vector"
  [articles]
  (for [{:keys [category content id] :as article} articles]
    (assoc article
           :uri (str "/" (name category) "/" (name id) "/")
           ;; I could render right here or I could just add a render function.
           ;; Having a render function makes it much easier to inspect the full
           ;; map output of this function in the repl
           :render-markdown (fn render-markdown []
                              (if-let [content-resource (io/resource content)]
                                (md-to-html-string (slurp content-resource))
                                (throw (ex-info "Missing content" {:article article})))))))

(defn realize-site
  "Creates fully realized site datastructure."
  ([] (realize-site (get-config)))
  ([config]
   (-> config
       (update :articles realize-articles)
       ;; Add tag indicies
       (assoc :tag-indicies (create-tag-indices config))
       ;; Add category indicies
       (assoc :category-indicies (create-category-indices config)))))

(comment (realize-site))

