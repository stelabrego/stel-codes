(ns stelcodes.dev-blog.generator
  (:gen-class)
  (:require [stasis.core :as stasis]
            [stelcodes.dev-blog.views :as views]
            [stelcodes.dev-blog.state :as state]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [stelcodes.optimus-sass.core]
            [optimus.prime :as optimus]
            [optimus.assets :as assets]
            [optimus.optimizations :as optimizations]
            [optimus.strategies :refer [serve-live-assets]]
            [optimus.export]
            [taoensso.timbre :as timbre :refer [info]]))

(defn generate-index
  [pages]
  (let [grouped-pages (group-by :type pages)]
    (->> pages
         (map (fn [page]
                {(:uri page) (fn [_] (views/render page grouped-pages))}))
         (into {}))))

(defn get-assets [] (assets/load-assets "public" [#"assets/.*"]))

(defn development-ring-app
  []
  (-> (stasis/serve-pages #(generate-index (state/get-preview-pages)))
      (optimus/wrap get-assets optimizations/all serve-live-assets)
      wrap-content-type))

(defn generic-export
  ([site-index target-dir]
   (let [assets (optimizations/all (get-assets) {})]
     (info "Building site...")
     (stasis/empty-directory! target-dir)
     (optimus.export/save-assets assets target-dir)
     (stasis/export-pages site-index target-dir {:optimus-assets assets})
     (info "Build successful")
     (info (str "Located in " target-dir)))))

(defn export-published
  []
  (generic-export (generate-index (state/get-published-pages))
                  "/www/dev-blog-published"))

(defn export-preview
  []
  (generic-export (generate-index (state/get-preview-pages))
                  "/www/dev-blog-preview"))

(defn export ([] (export-published) (export-preview)) ([_] (export)))

(comment
  (export-published)
  (export-preview))
