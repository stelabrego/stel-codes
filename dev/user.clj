(ns user
  (:require [clojure.repl :as repl]
            [ring.middleware.resource :refer [wrap-resource]]
            [stasis.core :as stasis]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [codes.stel.dev-blog.generator :as generator]
            [org.httpkit.server :refer [run-server]]
            [taoensso.timbre :as log]))

(defn dev-app
  []
  (-> (stasis/serve-pages #(generator/generate-page-index))
      (wrap-resource "public")
      wrap-content-type))

(defn start
  []
  (let [port 5775]
    (log/info "✨ Starting dev server on port" port)
    (run-server (dev-app) {:port port})))
