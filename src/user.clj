(ns user
  (:require [clojure.pprint :refer [pprint]]
            [clojure.repl :as repl]
            [nuzzle.api :as nuzz]))

(defn serve [] (nuzz/serve))

(defn realize [] (pprint (nuzz/realize)))

(defn export [] (nuzz/export :remove-drafts? true))
