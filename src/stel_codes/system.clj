(ns stel-codes.system
  (:require [integrant.core :as ig]
            [ring.adapter.jetty :as jetty]
            [stel-codes.generator :as generator]))

(def config
  {:adapter/jetty {:handler (ig/ref :handler/app)}
   :handler/app {}})

(defmethod ig/init-key :adapter/jetty [_ deps]
  (jetty/run-jetty (:handler deps) {:port 3000 :join? false}))

(defmethod ig/init-key :handler/app [_ _]
  generator/app)

;; this is why I'm using integrant here
(defmethod ig/halt-key! :adapter/jetty [_ server]
  (.stop server))

