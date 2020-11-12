(ns stel-codes.generator
  (:gen-class)
  (:require [stasis.core :as stasis]
            [stel-codes.views :as views]
            [ring.adapter.jetty :as jetty]))

(defn get-pages []
  {"/index.html" (views/home-page)})

(def target-dir "site")

(def site-config {})

(def app (stasis/serve-pages get-pages site-config))

(defn export
  ([]
   (println "Building site...")
   (stasis/empty-directory! target-dir)
   (stasis/export-pages (get-pages) target-dir)
   (println (str "Build successful.\nLocated in: " target-dir)))
  ([_]
   (export)))

(comment
  (serve)
  (export))
