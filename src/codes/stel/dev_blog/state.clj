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

(defn id->uri [{:keys [articles tag-indicies category-indicies]} id]
  (->> (concat articles tag-indicies category-indicies)
       (filter #(= id (:id %)))
       (first)
       (:uri)))

(comment
 (concat [1 2 3])
 (id->uri {:articles [{:id :poop :uri "/poop"}]} :poop))

(defn realize-site
  "Creates fully realized site datastructure with or without drafts."
  ([] (realize-site (get-config) true))
  ([include-drafts?] (realize-site (get-config) include-drafts?))
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
       (assoc $ :category-indicies (create-category-indices $))
       ;; Add function to get uri of any page easily
       (assoc $ :id->uri (partial $ id->uri)))))

(comment (realize-site))
