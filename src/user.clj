(ns user
  #_{:clj-kondo/ignore [:use]}
  (:use clojure.repl)
  (:require
   [nuzzle.api :as nuzz]))

(defn serve [] (nuzz/serve :nuzzle/build-drafts? true))

(defn diff [] (nuzz/transform-diff))

(defn publish [] (nuzz/publish))
