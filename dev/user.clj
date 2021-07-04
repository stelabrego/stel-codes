(ns user
  (:require [integrant.repl :as repl]
            [clojure.repl :refer [doc find-doc apropos source]]
            ; [integrant.core :as ig]
            [codes.stel.dev-blog.system :as system]))

(repl/set-prep! (fn [] system/config))

(def go repl/go)
(def halt repl/halt)
(def reset (fn [] (repl/halt) (repl/reset)))
(def reset-all repl/reset-all)

(go)

(comment
  (halt)
  (go)
  (reset)
  (reset-all))
