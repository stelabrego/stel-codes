(ns user
  (:require [clojure.repl :as repl]
            [codes.stel.dev-blog.views :as views]
            [codes.stel.nuzzle.core :as nuzzle]))

(def config {:site-config "edn/site.edn"
             :remove-drafts? true
             :static-dir "public"
             :render-fn views/render
             :target-dir "dist"})

(defn export []
  (nuzzle/export config))

(defn start []
  (nuzzle/start-server (assoc config :remove-drafts? false)))

