(ns stelabrego.stel-codes
  (:gen-class)
  (:require [stasis.core :as stasis])
  )

(defn get-pages []
  {"/index.html" "<h1>Welcome!</h1>"}
  )

(def target-dir "build")

(defn export []
  (stasis/empty-directory! target-dir)
  (stasis/export-pages (get-pages) target-dir))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Building site...")
  (export)
  )
