(ns stel-codes.generator
  (:gen-class)
  (:require [stasis.core :as stasis]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [stel-codes.views :as views]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [stelcodes.optimus-sass.core]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [optimus.export]
            [markdown.core :refer [md-to-html-string]]
            [camel-snake-kebab.core :as csk])

  (:import [java.time LocalDate]))
; (defrecord Page [uri title])

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn get-assets []
  (assets/load-assets "public" [#"assets/.*"]))

(defn string->page [content-string]
  (let [[_whole-string edn-string md-string] (re-matches #"(?s)(\{.*?\})(?:\s*)(.*)" content-string)
        html-string (md-to-html-string md-string)]
    (as-> (edn/read-string edn-string) $
      (assoc $ :body html-string)
      (assoc $ :uri (str "/" (csk/->kebab-case (symbol (:location $))) "/" (csk/->kebab-case (:title $)) "/"))
      (update $ :date (fn [date-string] (LocalDate/parse date-string))))))

(defn generate-journal-pages []
  (->>
   (.listFiles (io/file "resources/content/"))
   (map slurp)
   (map string->page)))

(defn tag-in-page? [tag page]
  (in? (:tags page) tag))

(defn generate-tag-pages [journal-pages]
  (let [tags (mapcat :tags journal-pages)]
    (for [tag tags]
      {:title (str "#" tag)
       :location :tag
       :uri (str "/tags/" tag "/")
       :articles (filter (partial tag-in-page? tag))})))

(comment
  (generate-tag-pages (generate-journal-pages)))

(defn generate-general-pages [journal-pages]
  (list
   {:location :home :uri "/" :journal-pages journal-pages}
   {:location :404 :uri "/404/" :title "404"}))

(defn generate-index []
  (let [journal-pages (generate-journal-pages)
        tag-pages (generate-tag-pages journal-pages)
        general-pages (generate-general-pages journal-pages)]
    (->>
   (concat journal-pages tag-pages general-pages)
   (remove :hidden)
   (map (fn [page] {(:uri page) (fn [_] (views/render-page page))}))
   (into {}))))

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

(comment
  (generate-index)
  (export))
