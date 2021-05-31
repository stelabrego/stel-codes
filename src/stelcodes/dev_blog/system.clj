(ns stelcodes.dev-blog.system
  (:require [integrant.core :as integrant]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.resource :refer [wrap-resource]]
            [stasis.core :as stasis]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [stelcodes.dev-blog.generator :as generator]
            [stelcodes.dev-blog.state :as state]))

(def development-ring-app
  (-> (stasis/serve-pages #(generator/generate-index (state/get-preview-pages)))
      (wrap-resource "public")
      wrap-content-type))

(def config {:adapter/jetty {:handler development-ring-app}})

(defmethod integrant/init-key :adapter/jetty [_ deps] (jetty/run-jetty (:handler deps) {:port 3000, :join? false}))

;; this is why I'm using integrant here
(defmethod integrant/halt-key! :adapter/jetty [_ server] (.stop server))

