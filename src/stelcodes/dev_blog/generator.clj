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
            [optimus.export]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as str])
  (:import [java.time LocalDate]
           [org.commonmark.parser Parser]
           [org.commonmark.renderer.html HtmlRenderer]))

; (defn ->slug [input]
;   (csk/->kebab-case (str/replace (str input) #"[^\w\s]" "")))

; (comment
;   (->slug "Wow this is / a weird !!!! ?? title for a note *"))

; (def type->uri {:project-note "/cool-stuff-like/"
;                 :learning-note "/and-learns-from/"
;                 :blog-note "/and-blogs-about/"})

;; https://mrmcc3.github.io/blog/posts/commonmark-in-clojure/
; (def parser (.build (Parser/builder)))
; (def renderer (.build (HtmlRenderer/builder)))

; (defn raw-string->note [content-string]
;   (let [[_ edn-string md-string] (re-matches #"(?s)(\{.*?\})(?:\s*)(.*)" content-string)
;         document (.parse parser md-string)
;         html-string (.render renderer document)]
;     (as-> (edn/read-string edn-string) $
;       (assoc $ :body html-string)
;       (assoc $ :uri (str ((:type $) type->uri) (->slug (:title $)) "/"))
;       (update $ :date (fn [date-string] (LocalDate/parse date-string))))))

; (defn read-notes []
;   (->>
;    (.listFiles (io/file "resources/notes/"))
;    (map slurp)
;    (map raw-string->note)))

; (comment
;   (read-notes))


; (comment
;   (generate-tag-pages (read-notes)))

(def general-pages
  (list
   {:type :home :uri "/"}
   {:type :404 :uri "/404.html"}))

(defn generate-index []
    (let [pages (concat general-pages (state/get-pages))
          grouped-pages (group-by :type pages)]
      (->>
      (map (fn [page] {(:uri page) (fn [_] (views/render page grouped-pages))}) pages)
      (into {}))))

(comment
  (generate-index))

(defn get-assets []
  (assets/load-assets "public" [#"assets/.*"]))

(def app (->
          (stasis/serve-pages generate-index)
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
  ([_]
   (export)))

(defn -main []
  (export))

(comment
  (generate-index)
  (export))
