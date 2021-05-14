(ns stelcodes.dev-blog.views
  (:require [hiccup.page :as hp]
            [hiccup2.core :refer [html raw]]
            [hiccup.element :as he]
            [stelcodes.dev-blog.state :as state]
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
  [:footer [:p "made by stel abrego with clojure on occupied " (he/link-to "https://native-land.ca/maps/territories/meskwahki%c2%b7asa%c2%b7hina-fox/" "meškwahki land")]])

(defn window [title body] [:section.window
                           [:div.top
                            (raw (slurp "resources/svg/bars.svg"))
                            [:span.title title]
                            (raw (slurp "resources/svg/bars.svg"))]
                           [:div.content body]])

(defn tag-group [tags]
  [:p.tags (for [tag tags] (he/link-to {:class "tag"} (state/tag-name->index-uri tag) (str "#" tag " ")))])

(defn window-list-item [item]
  (list (he/link-to {:class "title"} (:uri item) (:title item))
        (when-let [subtitle (:subtitle item)] [:p.pitch subtitle])
        (when-let [tags (:tags item)] (tag-group tags))))

(comment
  "old types"
  :note [:uri :title :pitch :tags :type :hidden :repo :body]
  "now I'm going to write out the new types in psuedo spec"
  :general-information [:introduction :twitter-uri :github-uri]
  :blog-post           [:id :date-created :date-updated :sort :status :tags :slug :title :subtitle :body]
  :educational-media   [:id :date-created :date-updated :sort :status :tags :slug :title :subtitle :body :rating]
  :coding-projects     [:id :date-created :date-updated :sort :status :tags :slug :title :subtitle :body :production_uri :repository_uri])

(defn home-content-window [title more-uri pages]
  (let [page-count (count pages)]
    (when-not (empty? pages)
      (window
       title
       (list
        (->>
         pages
         (sort-by :date)
         (reverse)
         (take 5)
         (map window-list-item)
         (he/unordered-list))
        (when (> page-count 5) (he/link-to {:class "more-link"} more-uri "more!")))))))

(defn welcome-section []
  [:section.welcome
   (he/image {:class "avatar"} "/assets/img/selfie3.jpg")
   [:span.name "Stel Abrego"]
   [:div.text
    [:p "Hi! I'm a freelance software engineer with a focus on functional design and web technologies."]
    [:p "Check out my projects, learning resources, and blog posts."]
    [:p "I also offer virtual tutoring for coding students. Please message me if you're interested."]]])

(defn layout [{:keys [title type]} & content]
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
         [:body (header) [:main {:class type} content] (footer)])
   (str)))

(defn render-generic [page]
  (layout page
          (window (:title page)
                  [:article
                   [:header
                    [:h1 (:title page)]
                    (when (not-empty (:tags page)) (tag-group (:tags page)))]
                   (when (:repo page) [:span (he/link-to (str "https://github.com/stelcodes/" (:repo page)) "🔧 Source Code")])
                   (raw (:body page))])))

(defn render-generic-index [page]
  (let [indexed-pages (:indexed-pages page)]
    (layout page
            (list
             (welcome-section)
             (window (:title page)
                     (he/unordered-list (map window-list-item indexed-pages)))))))

(defmulti render :type)

(defmethod render :default [page all-pages]
  (render-generic page))

(defmethod render :index [page all-pages]
  (render-generic-index page))

(defmethod render :home [page all-pages]
  (let [coding-project-pages (:coding-projects all-pages)
        educational-media-pages (:educational-media all-pages)
        blog-post-pages (:blog-posts all-pages)]
    (layout page
            (list
             (welcome-section)
             (when coding-project-pages (home-content-window "my coding projects" "/coding-projects/" coding-project-pages))
             (when educational-media-pages (home-content-window "my learning resources" "/and-learns-from/" educational-media-pages))
             (when blog-post-pages (home-content-window "my blog posts" "//" blog-post-pages))))))

(defmethod render :404 [page all-pages]
  (layout page
          [:h1 "404 ;-;"]))
