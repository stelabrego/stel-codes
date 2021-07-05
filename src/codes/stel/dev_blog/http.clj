(ns codes.stel.dev-blog.http
  (:require [org.httpkit.server :refer [run-server]]
            [taoensso.timbre :as timbre :refer [info]]
            [codes.stel.dev-blog.config :refer [config]]
            [codes.stel.dev-blog.generator :refer [export]]))

(defn app [_] (info "Webhook request received. Initiating build...") (export) {:status 200})

(defn listen
  ([]
   (let [http-config (:http config)
         port (:port http-config)]
     (info (str "Listening for webhooks on port " port))
     (run-server app http-config)))
  ([_] (listen)))
