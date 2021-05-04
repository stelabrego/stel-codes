(ns stelcodes.dev-blog.system
  (:require [integrant.core :as integrant]
            [ring.adapter.jetty :as jetty]
            [next.jdbc :as jdbc]
            [stelcodes.dev-blog.generator :as generator]))

(def db-spec {:dbtype "postgresql" :dbname "dev_blog" :host "127.0.0.1" :port 5432 :user "static_site_builder"})

(def config
  {:adapter/jetty {:handler (integrant/ref :handler/app)}
   :handler/app {:conn (integrant/ref :db/connection)}
   :db/connection {:db-spec db-spec}})

(defmethod integrant/init-key :adapter/jetty [_ deps]
  (jetty/run-jetty (:handler deps) {:port 3000 :join? false}))

(defmethod integrant/init-key :handler/app [_ _]
  generator/app)

(defmethod integrant/init-key :db/connection [_ deps]
  (jdbc/get-datasource (:db-spec deps)))

;; this is why I'm using integrant here
(defmethod integrant/halt-key! :adapter/jetty [_ server]
  (.stop server))
