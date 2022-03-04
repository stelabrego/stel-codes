(ns user
  (:require [clojure.pprint :refer [pprint]]
            [clojure.repl :as repl]
            [codes.stel.dev-blog.views :as views]
            [codes.stel.nuzzle.api :as nuzzle]))

(def config {:site-config "edn/site.edn"
             :static-dir "public"
             :render-page views/render-page
             :target-dir "dist"
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


