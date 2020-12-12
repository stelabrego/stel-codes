(ns stel-codes.views
  (:require [hiccup.page :as hp]
            [hiccup2.core :refer [html raw]]
            [hiccup.element :as he]
            [clojure.string :refer [starts-with?]]
            [hiccup.form :as hf]))

(defn layout [{:keys [title]} & content]
  (->
   (html {:lang "en"}
         [:head
          [:title
           (if title (str title " | stel.codes") "stel.codes")]
          [:meta {:charset "utf-8"}]
          [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
          [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
          (hp/include-css "/assets/reset.css")
          (hp/include-css "/assets/main.css")
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

(defn header []
  [:header
   [:nav
    (he/link-to {:id "logoLink"} "/" (raw (slurp "resources/svg/code.svg")))
    (he/link-to {:id "navName"} "/" "stel.codes")
    [:div#navBar.navBar-closed
     (he/unordered-list
      {:class "nav-ul-closed"}
      [(he/link-to "/projects" "Projects")
       (he/link-to "/resume" "Resume")
       (he/link-to "/tutoring" "Tutoring")
       (he/mail-to "stel@stel.codes" "E-Mail")])]]])

(defn page->view-category [{:keys [uri]}]
  (cond
    (= uri "/index.html") :home
    (= uri "/404.html") :404
    (= uri "/projects/index.html") :project-index
    (starts-with? uri "/projects/") :project
    :else (throw (Exception. (str "Cannot find view for uri:" uri)))))

(defmulti render-page page->view-category)

(defmethod render-page :project [page-data]
  (layout page-data
          [:main
           [:h1 (:title page-data)]
           (when (:tags page-data)
             [:div.code-tag-container
              (for [tag (:tags page-data)]
                [:div.code-tag tag])])]))

(defmethod render-page :project-index [page-data]
  (layout page-data
          (header)
          [:main
           [:h1 (:title page-data)]
           (->>
             (filter #(= :project (page->view-category %)) (:markup-pages page-data))
             (map (fn [project] [:h2 (:title project)])))]))

(defmethod render-page :home [page-data]
  (layout page-data
          (header)
          [:section.slideInUp
           [:p "programming tutor."]
           [:p "musician."]
           [:p "fullstack developer."]
           [:img.hero-selfie {:src "/assets/img/selfie.jpeg"}]]))

(defmethod render-page :404 [page-data]
  (layout page-data
          (header)
          [:h1 "404 ;-;"]))
