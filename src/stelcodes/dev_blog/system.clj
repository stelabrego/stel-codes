(ns stelcodes.dev-blog.system
  (:require [integrant.core :as integrant]
            [ring.adapter.jetty :as jetty]
            [stasis.core :as stasis]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [stelcodes.dev-blog.generator :as generator]
            [stelcodes.dev-blog.state :as state]))

(def config
  {:adapter/jetty {:handler (integrant/ref :handler/app)}, :handler/app nil})

(defn development-ring-app
  []
  (-> (stasis/serve-pages #(generator/generate-index (state/get-preview-pages)))
      wrap-content-type))

(defmethod integrant/init-key :adapter/jetty
  [_ deps]
  (jetty/run-jetty (:handler deps) {:port 3000, :join? false}))

;; this is why I'm using integrant here
(defmethod integrant/halt-key! :adapter/jetty [_ server] (.stop server))

(defmethod integrant/init-key :handler/app
  [_ _]
  (generator/development-ring-app))

