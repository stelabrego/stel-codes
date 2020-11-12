(ns stel-codes.views
  (:require [hiccup.page :as hp]
            [hiccup2.core :refer [html]]
            [hiccup.element :as he]
            [hiccup.form :as hf]))

(defn layout [title & content]
  (->
   (html {:lang "en"}
         [:head
          [:title title]
          [:meta {:charset "utf-8"}]
          [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
          ; [:link {:rel "shortcut icon" :href "/assets/favicon.ico" :type "image/x-icon"}]
          ; (hp/include-css "/assets/minireset.css")
          ; (hp/include-css "/assets/main.css")
          ; (if (= (System/getenv "PROD") "true")
          ;   [:script
          ;    {:src "https://plausible.io/js/plausible.js",
          ;     :data-domain "cuter-news.herokuapp.com",
          ;     :defer "defer",
          ;     :async "async"}])
          ]
         [:body content])
   (str)))

(defn home-page []
  (layout "stel.codes"
          [:h1 "suppy sups"]))
