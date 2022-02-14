(ns codes.stel.dev-blog.generator
  (:require [stasis.core :as stasis]
            ;; [codes.stel.dev-blog.views :as views]
            [codes.stel.dev-blog.state :as state]
            [clojure.java.io :as io]
            [me.raynes.fs :refer [copy-dir]]
            [taoensso.timbre :as timbre :refer [info]]))

(defn generate-index
  ([] (generate-index (state/realize-prod-site)))
  ([{:keys [articles tag-indicies category-indicies] :as realized-site}]
   (->> [{:uri "/" :category :home} {:uri "/404" :category :404}]
        (concat articles tag-indicies category-indicies)
        ;; TODO change back to views/render
        (map #(vector (:uri %) (fn [_] (println % realized-site))))
        (into {}))))

(comment (generate-index))

(defn generic-export
  ([site-index target-dir]
   (let [assets (io/file (io/resource "public/assets"))]
     (info "Building site...")
     (stasis/empty-directory! target-dir)
     (stasis/export-pages site-index target-dir)
     (copy-dir assets target-dir)
     (info "Build successful")
     (info (str "Located in " target-dir)))))

(defn export-prod []
  (generic-export (generate-index (state/realize-prod-site)) "/dist/dev"))

(defn export-dev []
  (generic-export (generate-index (state/realize-dev-site)) "/dist/prod"))

(defn export
  ([] (export-prod) (export-dev))
  ([_] (export)))

;; (comment
;;   (export)
;;   (export-published)
;;   (export-preview))
