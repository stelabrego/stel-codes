(ns stel-codes.views
  (:require [hiccup.page :as hp]
            [hiccup2.core :refer [html raw]]
            [hiccup.element :as he]
            [clojure.string :refer [starts-with?]]
            [hiccup.form :as hf]))

(def spy #(do (println "DEBUG:" %) %))

(defn header []
  [:header
   [:nav
    (he/link-to {:id "brand"} "/" (raw (slurp "resources/svg/rainbow-apple.svg") "stel.codes"))
    (he/unordered-list
     {:id "social"}
     [(he/link-to "https://github.com/stelcodes" "Github")
      (he/link-to "https://twitter.com/stel_codes" "Twitter")
      (he/mail-to "stel@stel.codes" "Email")])]])

(defn footer []
  [:footer [:p "made by stel abrego with clojure while living on " (he/link-to "https://native-land.ca/maps/territories/meskwahki%c2%b7asa%c2%b7hina-fox/" "stolen miskwaki territory")]])

(defn window [title content]
  [:section.window
   [:div.top
    (raw (slurp "resources/svg/bars.svg"))
    [:span title]
    (raw (slurp "resources/svg/bars.svg"))]
   [:div.content content]])

(defn home-content-window [title pages]
  (window
   title
   (list
    (->>
     pages
     (spy)
     (sort-by :date)
     (reverse)
     (take 5)
     (map (fn [project] (list (he/link-to (:uri project) (:title project))
                              (when-let [pitch (:pitch project)] [:p.pitch pitch])
                              (when-let [tags (:tags project)]
                                [:p.tags (for [tag tags] [:span.tag (str "#" tag " ")])]))))
     (he/ordered-list))
    (he/link-to {:class "more-link"} (str "/" title) (str "more " title)))))

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
         [:body (header) [:main content] (footer)])
   (str)))

(defn page->view-category [{:keys [uri]}]
  (cond
    (= uri "/index.html") :home
    (= uri "/404.html") :404
    (= uri "/projects/index.html") :project-index
    (starts-with? uri "/projects/") :project
    (starts-with? uri "/reading/") :reading
    :else (throw (Exception. (str "Cannot find view for uri:" uri)))))

(defmulti render-page page->view-category)

(defmethod render-page :project [page-data]
  (layout page-data
          [:h1 (:title page-data)]
          (when (:tags page-data)
            [:div.code-tag-container
             (for [tag (:tags page-data)]
               [:div.code-tag tag])])))

(defmethod render-page :reading [page-data]
  (layout page-data
          [:h1 (:title page-data)]
          (when (:tags page-data)
            [:div.code-tag-container
             (for [tag (:tags page-data)]
               [:div.code-tag tag])])))

(defmethod render-page :project-index [page-data]
  (layout page-data
          [:h1 (:title page-data)]
          (->>
           (:markup-pages page-data)
           (filter #(= :project (page->view-category %)))
           (sort-by :date)
           (map (fn [project] [:h2 (:title project)])))))

(defmethod render-page :home [page-data]
  (let [project-pages (filter #(= :project  (page->view-category %)) (:markup-pages page-data))
        reading-pages (filter #(= :reading  (page->view-category %)) (:markup-pages page-data))]
    (run! print project-pages)
    (layout page-data
            (list
             (home-content-window "projects" project-pages)
             (home-content-window "reading" reading-pages)))))

(defmethod render-page :404 [page-data]
  (layout page-data
          [:h1 "404 ;-;"]))
