(ns codes.stel.dev-blog.config
  (:require [cprop.core :refer [load-config]]))

(def config (load-config :resource "config/config.edn"))
