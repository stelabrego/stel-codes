(ns stelcodes.dev-blog.generator
  (:gen-class)
  (:require [stasis.core :as stasis]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [stelcodes.dev-blog.views :as views]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [stelcodes.optimus-sass.core]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [optimus.export]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as str])
  (:import [java.time LocalDate]
           [org.commonmark.parser Parser]
           [org.commonmark.renderer.html HtmlRenderer]))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn ->slug [input]
  (csk/->kebab-case (str/replace (str input) #"[^\w\s]" "")))

(comment
  (->slug "Wow this is / a weird !!!! ?? title for a note *"))

(def type->uri {:project-note "/cool-stuff-like/"
                :learning-note "/and-learns-from/"
                :blog-note "/and-blogs-about/"})

;; https://mrmcc3.github.io/blog/posts/commonmark-in-clojure/
(def parser (.build (Parser/builder)))
(def renderer (.build (HtmlRenderer/builder)))

(defn raw-string->note [content-string]
  (let [[_ edn-string md-string] (re-matches #"(?s)(\{.*?\})(?:\s*)(.*)" content-string)
        document (.parse parser md-string)
        html-string (.render renderer document)]
    (as-> (edn/read-string edn-string) $
      (assoc $ :body html-string)
      (assoc $ :uri (str ((:type $) type->uri) (->slug (:title $)) "/"))
      (update $ :date (fn [date-string] (LocalDate/parse date-string))))))

(defn read-notes []
  (->>
   (.listFiles (io/file "resources/notes/"))
   (map slurp)
   (map raw-string->note)))

(comment
  (read-notes))

(defn tag-in-note? [tag note]
  (in? (:tags note) tag))

(defn generate-tag-pages [notes]
  (let [tags (->>
              notes
              (remove :hidden)
              (mapcat :tags)
              (set))]
    (for [tag tags]
      {:title (str "#" tag)
       :type :i/tag
       :uri (str "/tags/" tag "/")
       :note-index (filter (partial tag-in-note? tag) notes)})))

(comment
  (generate-tag-pages (read-notes)))

(defn generate-general-pages []
  (list
   {:type :home :uri "/"}
   {:type :i/project-note :title "coding projects" :uri "/cool-stuff-like/"}
   {:type :i/learning-note :title "learning resources" :uri "/and-learns-from/"}
   {:type :404 :uri "/404.html"}))

(comment
  (generate-general-pages))

(defn generate-index []
  (let [notes (read-notes)
        tag-pages (generate-tag-pages notes)
        general-pages (generate-general-pages)]
    (as-> (concat notes tag-pages general-pages) $
      (map #(assoc % :notes $) $)
      (remove :hidden $)
      (map (fn [page] {(:uri page) (fn [_] (views/render page))}) $)
      (into {} $))))

(comment
  (generate-index))

(defn get-assets []
  (assets/load-assets "public" [#"assets/.*"]))

(def app (->
          (stasis/serve-pages generate-index)
          (optimus/wrap get-assets optimizations/all serve-live-assets)
          wrap-content-type))

(defn export
  ([]
   (let [assets (optimizations/all (get-assets) {})
         pages (generate-index)
         target-dir "site"]
     (println "Building site...")
     (stasis/empty-directory! target-dir)
     (optimus.export/save-assets assets target-dir)
     (stasis/export-pages pages target-dir {:optimus-assets assets})
     (println (str "Build successful.\nLocated in: " target-dir))))
  ([_]
   (export)))

(defn -main []
  (export))

(comment
  (generate-index)
  (export))
