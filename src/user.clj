(ns user
  (:require [clojure.pprint :refer [pprint]]
            [clojure.repl :as repl]
            [views :as views]
            [codes.stel.nuzzle.api :as nuzzle]))

(def config {:site-data "edn/site.edn"
             :static-dir "static"
             :render-webpage views/render-webpage
             :rss-opts
             {:author "stel@stel.codes (Stel Abrego)"
              :title "Stel Codes"
              :description "Thoughts about programming and whatever else."
              :link "https://stel.codes"}})

(defn start []
  (nuzzle/start-server config))

(defn inspect []
  (pprint (nuzzle/inspect config)))

(defn export []
  (nuzzle/export (assoc config :remove-drafts? true)))


