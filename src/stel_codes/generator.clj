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
            [camel-snake-kebab.core :as csk]
            [clojure.string :as str]
            [tupelo.core :as t]
            [tupelo.base64url :refer [encode-str]])
  (:import [java.time LocalDate]))
; (defrecord Page [uri title])

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn get-assets []
  (assets/load-assets "public" [#"assets/.*"]))

(defn ->slug [input]
  (csk/->kebab-case (str/replace (str input) #"[^\w\s]" "")))

(comment
  (->slug "Wow this is / a weird !!!! ?? title for a note *"))

(defn string->note-data [content-string]
  (let [[_whole-string edn-string md-string] (re-matches #"(?s)(\{.*?\})(?:\s*)(.*)" content-string)
        html-string (md-to-html-string md-string)]
    (as-> (edn/read-string edn-string) $
      (assoc $ :body html-string)
      (assoc $ :uri (str "/" (->slug (symbol (:type $))) "/" (->slug (:title $)) "/"))
      (update $ :date (fn [date-string] (LocalDate/parse date-string))))))

(defn generate-notes []
  (->>
   (.listFiles (io/file "resources/notes/"))
   (map slurp)
   (map string->note-data)))

(comment
  (generate-notes)
  )

(defn tag-in-page? [tag page]
  (in? (:tags page) tag))

(defn generate-tag-pages [journal-pages]
  (let [tags (mapcat :tags journal-pages)]
    (for [tag tags]
      {:title (str "#" tag)
       :type :i/tag
       :uri (str "/tags/" tag "/")
       :index (filter (partial tag-in-page? tag) journal-pages)})))

(comment
  (generate-tag-pages (generate-notes)))

(defn generate-general-pages []
  (list
   {:type :home :uri "/"}
   {:type :i/project-note :title "coding projects" :uri "/cool-stuff-like/"}
   {:type :404 :uri "/404.html"}))

(comment
  (generate-general-pages))

(defn generate-index []
  (let [journal-pages (generate-notes)
        tag-pages (generate-tag-pages journal-pages)
        general-pages (generate-general-pages)]
    (as-> (concat journal-pages tag-pages general-pages) $
      #_(print $)
      (map #(assoc % :all-notes $) $)
      (remove :hidden $)
      (map (fn [page] {(:uri page) (fn [_] (views/render page))}) $)
      (into {} $))))

(comment
  (generate-index))

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
