(ns codes.stel.dev-blog.config
  (:require [cprop.core :refer [load-config]]
            ;; [clojure.java.io :as io]
            [clojure.edn :as edn]))

(def config (load-config :resource "config/config.edn"))

(defn get-config []
  (edn/read-string (slurp "config.edn")))
