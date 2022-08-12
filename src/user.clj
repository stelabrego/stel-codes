(ns user
  (:use clojure.repl)
  (:require [clojure.pprint :refer [pprint]]
            [clojure.repl :as repl]
            [nuzzle.api :as nuzz]))

(defn serve [] (nuzz/serve :nuzzle/build-drafts? true))

(defn realize [] (pprint (nuzz/realize)))

(defn publish [] (nuzz/publish))
