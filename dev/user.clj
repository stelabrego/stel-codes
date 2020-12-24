(ns user
  (:require [integrant.repl :as repl]
            ; [integrant.core :as ig]
            [stelcodes.dev-website.system :as system]
            [cljfmt.main :as cljfmt]))

(repl/set-prep! (fn [] system/config))

(def go repl/go)
(def halt repl/halt)
(def reset repl/reset)
(def reset-all repl/reset-all)

(go)

(comment
  (go)
  (reset)
  (reset-all)
  (cljfmt/-main "fix" "src" "deps.edn" "test" "dev"))
