(ns stel-codes.generator
  (:gen-class)
  (:require [stasis.core :as stasis]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [stel-codes.views :as views]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [optimus.export]
            [markdown.core :refer [md-to-html-string]])

  (:import [java.time LocalDate]))
; (defrecord Page [uri title])

(defn get-assets []
  (assets/load-assets "public" [#"assets/.*"]))

(defn string->page [content-string]
  (let [[_whole-string edn-string md-string] (re-matches #"(?s)(\{.*\})(?:\W*)(.*)" content-string)
        html-string (md-to-html-string md-string)]
    (->
     (edn/read-string edn-string)
     (assoc :body html-string))))

(defn generate-markup-pages []
  (->>
   (.listFiles (io/file "resources/content/"))
   (map slurp)
   (map string->page)))

(defn generate-all-pages []
  (let [markup-pages (generate-markup-pages)]
    (conj
     markup-pages
     {:category :home :uri "/index.html" :markup-pages markup-pages}
     {:category :project-index :uri "/projects/index.html" :title "projects" :markup-pages markup-pages}
     {:category :404 :uri "/404.html" :title "404"})))

(defn generate-index []
  (->>
   (generate-all-pages)
   (map (fn [page] {(:uri page) (fn [_] (views/render-page page))}))
   (into {})))

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
  (load-content)
  (serve)
  (export))
