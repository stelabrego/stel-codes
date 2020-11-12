(ns stel-codes.generator
  (:gen-class)
  (:require [stasis.core :as stasis]
            [stel-codes.views :as views]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [optimus.export]))

(defn get-assets []
  (assets/load-assets "public" [#"assets/.*"]))

(defn get-pages []
  {"/index.html" (fn [context] (views/home-page context))})

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
  (serve)
  (export))
