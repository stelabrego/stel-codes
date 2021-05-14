(ns stelcodes.dev-blog.generator
  (:gen-class)
  (:require [stasis.core :as stasis]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [stelcodes.dev-blog.views :as views]
            [stelcodes.dev-blog.state :as state]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [stelcodes.optimus-sass.core]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [optimus.export]))

(def general-pages
  (list {:type :home, :uri "/"} {:type :404, :uri "/404.html"}))

(defn generate-index
  []
  (let [pages (concat general-pages (state/get-pages))
        grouped-pages (group-by :type pages)]
    (->> (map (fn [page]
                {(:uri page) (fn [_] (views/render page grouped-pages))})
           pages)
         (into {}))))

(comment
  (generate-index))

(defn get-assets [] (assets/load-assets "public" [#"assets/.*"]))

(def app
  (-> (stasis/serve-pages generate-index)
      (optimus/wrap get-assets optimizations/all serve-live-assets)
      wrap-content-type))

(defn export
  ([]
   (let [assets (optimizations/all (get-assets) {})
         site-index (generate-index)
         target-dir "site"]
     (println "Building site...")
     (stasis/empty-directory! target-dir)
     (optimus.export/save-assets assets target-dir)
     (stasis/export-pages site-index target-dir {:optimus-assets assets})
     (println (str "Build successful.\nLocated in: " target-dir))))
  ([_] (export)))

(defn -main [] (export))

(comment
  (generate-index)
  (export))
