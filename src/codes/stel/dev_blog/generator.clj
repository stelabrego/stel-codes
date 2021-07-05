(ns codes.stel.dev-blog.generator
  (:require [stasis.core :as stasis]
            [codes.stel.dev-blog.views :as views]
            [codes.stel.dev-blog.state :as state]
            [clojure.java.io :refer [resource file]]
            [me.raynes.fs :refer [copy-dir]]
            [taoensso.timbre :as timbre :refer [info]]))

(defn generate-index
  [pages]
  (let [grouped-pages (group-by :type pages)]
    (->> pages
         (map (fn [page] {(:uri page) (fn [_] (views/render page grouped-pages))}))
         (into {}))))

(comment
  (stasis/slurp-resources "public" #"")
  (generate-index (state/get-published-pages)))

(defn generic-export
  ([site-index target-dir]
   (let [assets (file (resource "public/assets"))]
     (info "Building site...")
     (stasis/empty-directory! target-dir)
     (stasis/export-pages site-index target-dir)
     (copy-dir assets target-dir)
     (info "Build successful")
     (info (str "Located in " target-dir)))))

(defn export-published [] (generic-export (generate-index (state/get-published-pages)) "/www/dev-blog-published"))

(defn export-preview [] (generic-export (generate-index (state/get-preview-pages)) "/www/dev-blog-preview"))

(defn export ([] (export-published) (export-preview)) ([_] (export)))

(comment
  (export)
  (export-published)
  (export-preview))
