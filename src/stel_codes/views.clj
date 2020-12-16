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
  [:footer [:p "made by stel abrego with clojure in " (he/link-to "https://native-land.ca/maps/territories/meskwahki%c2%b7asa%c2%b7hina-fox/" "meškwahki territory")]])

(defn window [title content]
  [:section.window
   [:div.top
    (raw (slurp "resources/svg/bars.svg"))
    [:span.title title]
    (raw (slurp "resources/svg/bars.svg"))]
   [:div.content content]])

(defn tag-html [tags]
  [:p.tags (for [tag tags] (he/link-to {:class "tag"} (str "/tags/" tag) (str "#" tag " ")))])

(defn article-listing-html [article]
  (list (he/link-to (:uri article) (:title article))
        (when-let [pitch (:pitch article)] [:p.pitch pitch])
        (when-let [tags (:tags article)]
          (tag-html tags))))

(defn home-content-window [title more-uri pages]
  (window
   title
   (list
    (->>
     pages
     (spy)
     (sort-by :date)
     (reverse)
     (take 5)
     (map article-listing-html)
     (he/ordered-list))
    (he/link-to {:class "more-link"} more-uri (str "more " title)))))

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

; (defn page->view-category [{:keys [uri]}]
;   (cond
;     (= uri "/index.html") :home
;     (= uri "/404.html") :404
;     (= uri "/cool-stuff/index.html") :project-index
;     (starts-with? uri "/cool-stuff/") :project
;     (starts-with? uri "/and-reads/") :reading
;     (starts-with? uri "/tags/") :tag
;     (starts-with? uri "/and-listens-to/") :podcast
;     :else (throw (Exception. (str "Cannot find view for uri:" uri)))))

(defmulti render-page :location)

(defmethod render-page :coding-journal [page-data]
  (layout page-data
          (window (:title page-data)
                  [:article (raw (:body page-data))])))

(defmethod render-page :podcast-journal [page-data]
  (layout page-data
          (window (:title page-data)
                  [:article (raw (:body page-data))])))

(defmethod render-page :tag [page-data]
  (layout page-data
          (window (:title page-data)
                  (he/ordered-list (map article-listing-html (:articles page-data))))))

(defmethod render-page :reading-journal [page-data]
  (layout page-data
          [:h1 (:title page-data)]
          (when (:tags page-data)
            [:div.code-tag-container
             (for [tag (:tags page-data)]
               [:div.code-tag tag])])))

(defmethod render-page :i/coding-journal [page-data]
  (layout page-data
          [:h1 (:title page-data)]
          (->>
           (:journal-pages page-data)
           (filter #(= :coding-journal (:location %)))
           (sort-by :date)
           (map (fn [project] [:h2 (:title project)])))))

(defmethod render-page :home [page-data]
  (let [journal-pages (:journal-pages page-data)
        coding-journal (filter #(= :coding-journal  (:location %)) journal-pages)
        reading-journal (filter #(= :reading-journal  (:location %)) journal-pages)
        podcast-journal (filter #(= :podcast-journal  (:location %)) journal-pages)]
    (layout page-data
            (list
             (home-content-window "coding journal" "/coding-journal/" coding-journal)
             (home-content-window "book journal" "/book-journal/" reading-journal)
             (home-content-window "podcast journal" "/podcast-journal/" podcast-journal)))))

(defmethod render-page :404 [page-data]
  (layout page-data
          [:h1 "404 ;-;"]))
