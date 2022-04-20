(ns user
  (:require [clojure.pprint :refer [pprint]]
            [clojure.repl :as repl]
            [views :as views]
            [nuzzle.api :as nuzzle]))

(def config {:site-data "edn/site.edn"
             :static-dir "static"
             :render-webpage views/render-webpage
             :chroma-style "dracula"
             :rss-opts
             {:author "stel@stel.codes (Stel Abrego)"
              :title "Stel Codes"
              :description "Thoughts about programming and whatever else."
              :link "https://stel.codes"}})

(defn start []
  (nuzzle/start-server config))

(defn realize []
  (pprint (nuzzle/realize config)))

(defn export []
  (nuzzle/export (assoc config :remove-drafts? true)))
