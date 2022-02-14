(ns user
  ;; (:require [clojure.repl :refer [doc find-doc apropos source]]
  ;;           [ring.middleware.resource :refer [wrap-resource]]
  ;;           [stasis.core :as stasis]
  ;;           [ring.middleware.content-type :refer [wrap-content-type]]
  ;;           [codes.stel.dev-blog.generator :as generator]
  ;;           [codes.stel.dev-blog.state :as state]
  ;;           [org.httpkit.server :refer [run-server]]
  ;;           [taoensso.timbre :as timbre :refer [info]]
  ;;           [codes.stel.dev-blog.config :refer [config]])

  )

;; (defn dev-app
;;   []
;;   (-> (stasis/serve-pages #(generator/generate-index (state/get-preview-pages)))
;;       (wrap-resource "public")
;;       wrap-content-type))
;;
;; (defn start
;;   []
;;   (let [dev-config (:dev config)
;;         dev-port (:port dev-config)]
;;     (info "Starting dev server on port " dev-port)
;;     (run-server (dev-app) dev-config)))
;;
