(ns codes.stel.dev-blog.webhook
  (:require [org.httpkit.server :refer [run-server]]
            [taoensso.timbre :as timbre :refer [info]]
            [codes.stel.dev-blog.generator :refer [export]]))

(def port 1201)

(defn app [_] (info "Webhook request received. Initiating build...") (export) {:status 200})

(defn listen ([] (info (str "Listening for webhooks on port " port)) (run-server app {:port port})) ([_] (listen)))
