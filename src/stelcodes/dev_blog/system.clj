(ns stelcodes.dev-blog.system
  (:require [integrant.core :as integrant]
            [ring.adapter.jetty :as jetty]
            [stelcodes.dev-blog.generator :as generator]))

(def config
  {:adapter/jetty {:handler (integrant/ref :handler/app)}, :handler/app nil})

(defmethod integrant/init-key :adapter/jetty
  [_ deps]
  (jetty/run-jetty (:handler deps) {:port 3000, :join? false}))

;; this is why I'm using integrant here
(defmethod integrant/halt-key! :adapter/jetty [_ server] (.stop server))

(defmethod integrant/init-key :handler/app
  [_ _]
  (generator/development-ring-app))

