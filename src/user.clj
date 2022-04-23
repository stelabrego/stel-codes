(ns user
  (:require [clojure.pprint :refer [pprint]]
            [clojure.repl :as repl]
            [nuzzle.api :as nuzzle]))

(defn start []
  (nuzzle/start-server))

(defn realize []
  (pprint (nuzzle/realize)))

(defn export []
  (nuzzle/export :remove-drafts? true))
