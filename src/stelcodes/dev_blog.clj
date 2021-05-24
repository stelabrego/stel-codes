(ns stelcodes.dev-blog
  (:require [stelcodes.dev-blog.generator :as generator])
  (:gen-class))

(defn -main [& _] (generator/export))
