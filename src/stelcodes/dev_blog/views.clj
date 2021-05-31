(ns stelcodes.dev-blog.views
  (:require [hiccup.page :as hp]
            [hiccup2.core :refer [html raw]]
            [hiccup.element :as he]
            [stelcodes.dev-blog.state :as state]
            [clojure.string :refer [starts-with?]]
            [hiccup.form :as hf]))

(def spy #(do (println "DEBUG:" %) %))

(defn header
  []
  (let [{:keys [twitter-uri github-uri email-address]} (state/get-general-information)]
    [:header
     [:nav (he/link-to {:id "brand"} "/" (he/image "https://s3.stel.codes/nixos-logo.png") [:span "stel.codes"])
      (he/unordered-list {:id "social"}
                         [(he/link-to github-uri "Github") (he/link-to twitter-uri "Twitter")
                          (he/mail-to email-address "Email")])]]))

(defn footer
  []
  [:footer
   [:p "made by stel abrego with clojure on occupied "
    (he/link-to "https://native-land.ca/maps/territories/meskwahki%c2%b7asa%c2%b7hina-fox/" "meškwahki land")]])

(defn window
  [title body]
  [:section.window
   [:div.top (raw (slurp "resources/svg/bars.svg")) [:span.title title] (raw (slurp "resources/svg/bars.svg"))]
   [:div.content body]])

(defn tag-group
  [tags]
  [:p.tags (for [tag tags] (he/link-to {:class "tag"} (state/tag-name->index-uri tag) (str "#" tag " ")))])

(defn window-list-item
  [item]
  (list (he/link-to {:class "title"} (:uri item) (:title item))
        (when-let [subtitle (:subtitle item)] [:p.pitch subtitle])
        (when-let [tags (:tags item)] (tag-group tags))))

(comment
  "old types"
  :note
  [:uri :title :pitch :tags :type :hidden :repo :body]
  "now I'm going to write out the new types in psuedo spec"
  :general-information [:introduction :twitter-uri :github-uri]
  :blog-post [:id :date-created :date-updated :sort :status :tags :slug :title :subtitle :body]
  :educational-media [:id :date-created :date-updated :sort :status :tags :slug :title :subtitle :body :rating]
  :coding-projects [:id :date-created :date-updated :sort :status :tags :slug :title :subtitle :body :production_uri
                    :repository_uri])

(defn home-content-window
  [title more-uri pages]
  (let [page-count (count pages)]
    (when-not (empty? pages)
      (window title
              (list (->> pages
                         (sort-by :date)
                         (reverse)
                         (take 5)
                         (map window-list-item)
                         (he/unordered-list))
                    (when (> page-count 5) (he/link-to {:class "more-link"} more-uri "more!")))))))

(defn welcome-section
  []
  (let [{:keys [introduction]} (state/get-general-information)]
    [:section.welcome (he/image {:class "avatar"} "https://s3.stel.codes/avatar.png")
     [:span.name "Stel Abrego, Software Developer"] (raw introduction)]))

(defn layout
  [{:keys [title type]} & content]
  (-> (html {:lang "en"}
            [:head [:title (if title (str title " | stel.codes") "stel.codes")] [:meta {:charset "utf-8"}]
             [:meta {:http-equiv "X-UA-Compatible", :content "IE=edge"}]
             [:meta {:name "viewport", :content "width=device-width, initial-scale=1.0"}]
             (hp/include-css "/assets/css/main.css")
             ; [:link {:rel "shortcut icon" :href "/assets/favicon.ico" :type
             ; "image/x-icon"}]
             ; (hp/include-css "/assets/minireset.css")
             ; (hp/include-css "/assets/main.css")
             ; (if (= (System/getenv "PROD") "true")
             ;   [:script
             ;    {:src "https://plausible.io/js/plausible.js",
             ;     :data-domain "cuter-news.herokuapp.com",
             ;     :defer "defer",
             ;     :async "async"}])
            ]
            [:body (header) [:main {:class type} content] (footer)])
      (str)))

(defn render-generic
  [page]
  (layout page
          (window (state/kebab-case->title-case (name (:type page)))
                  [:article
                   [:header [:h1 (:title page)] (when (not-empty (:tags page)) (tag-group (:tags page)))
                    (when (:repo page)
                      [:span (he/link-to (str "https://github.com/stelcodes/" (:repo page)) "🔧 Source Code")])
                    (when-let [img (:header-image page)] (he/image img))] (raw (:body page))])))

(defn render-generic-index
  [page]
  (let [indexed-pages (:indexed-pages page)]
    (layout page
            (list (welcome-section) (window (:title page) (he/unordered-list (map window-list-item indexed-pages)))))))

(defmulti render :type)

(defmethod render :default [page all-pages] (render-generic page))

(defmethod render :index [page all-pages] (render-generic-index page))

(defn get-index-uri-for-pages
  [pages]
  (when pages
    (->> pages
         (first)
         (:uri)
         (re-find #"^/[^\/]*/"))))

(defmethod render :home
  [page all-pages]
  (let [coding-projects-pages (:coding-projects all-pages)
        coding-projects-index-uri (get-index-uri-for-pages coding-projects-pages)
        educational-media-pages (:educational-media all-pages)
        educational-media-index-uri (get-index-uri-for-pages educational-media-pages)
        blog-posts-pages (:blog-posts all-pages)
        blog-posts-index-uri (get-index-uri-for-pages blog-posts-pages)]
    (layout
      page
      (list (welcome-section)
            (when coding-projects-pages
              (home-content-window "my coding projects" coding-projects-index-uri coding-projects-pages))
            (when educational-media-pages
              (home-content-window "my learning resources" educational-media-index-uri educational-media-pages))
            (when blog-posts-pages (home-content-window "my blog posts" blog-posts-index-uri blog-posts-pages))))))

(defmethod render :404 [page all-pages] (layout page [:h1 "404 ;-;"]))
