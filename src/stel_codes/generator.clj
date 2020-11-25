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
            [optimus.export])
  (:import [java.time LocalDate]))

(defn get-assets []
  (assets/load-assets "public" [#"assets/.*"]))

(defn process-raw-content [item]
  (let [
        results (re-matches #"(?s)(\{.*\})(?:\W*)(.*)" item)
        ]
    results
    ))

(defn load-content []
  (let [content-files (.listFiles (io/file "resources/content/"))
        content-raw (map slurp content-files)]

    (map process-raw-content content-raw)))

(defn get-portfolio-pages []
  (stasis/slurp-directory "resources/content/portfolio/" #"/.md$"))

(defn get-pages []
  (stasis/merge-page-sources
   {"/index.html" (fn [context] (views/home-page context))
    "/portfolio.html" (fn [context])}))

(def site-config {})

(def app (->
          (stasis/serve-pages get-pages site-config)
          (optimus/wrap get-assets optimizations/all serve-live-assets)
          wrap-content-type))

(defn export
  ([]
   (let [assets (optimizations/all (get-assets) {})
         pages (get-pages)
         target-dir "site"]
     (println "Building site...")
     (stasis/empty-directory! target-dir)
     (optimus.export/save-assets assets target-dir)
     (stasis/export-pages pages target-dir {:optimus-assets assets})
     (println (str "Build successful.\nLocated in: " target-dir))))
  ([_]
   (export)))

(comment
  (load-content)
  (serve)
  (export))
