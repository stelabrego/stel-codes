(ns user
  (:require [clojure.pprint :refer [pprint]]
            [clojure.repl :as repl]
            [codes.stel.dev-blog.views :as views]
            [codes.stel.nuzzle.api :as nuzzle]))

(def config {:site-config "edn/site.edn"
             :static-dir "public"
             :render-fn views/render
             :target-dir "dist"})

(defn start []
  (nuzzle/start-server config))

(defn inspect []
  (pprint (nuzzle/inspect config)))

(defn export []
  (nuzzle/export (assoc config :remove-drafts? true)))


