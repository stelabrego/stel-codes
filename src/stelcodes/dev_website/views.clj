(ns stelcodes.dev-website.views
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

(defn tag-group [tags]
  [:p.tags (for [tag tags] (he/link-to {:class "tag"} (str "/tags/" tag) (str "#" tag " ")))])

(defn note-index-item [note]
  (list (he/link-to (:uri note) (:title note))
        (when-let [pitch (:pitch note)] [:p.pitch pitch])
        (when-let [tags (:tags note)] (tag-group tags))))

(defn home-content-window [title more-uri pages]
  (let [page-count (count pages)] (window
   title
   (list
    (->>
     pages
     (sort-by :date)
     (reverse)
     (take 5)
     (map note-index-item)
     (he/unordered-list))
    (when (> page-count 5) (he/link-to {:class "more-link"} more-uri (str "more " title)))))))

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

(defn render-generic [note-data]
  (layout note-data
          (window (:title note-data)
                  [:article (raw (:body note-data))])))

(defn render-generic-index [note-data]
  (let [note-index (or (:note-index note-data)
                       (filter #(and (nil? (namespace (:type %)))
                                     (= (name (:type note-data)) (name (:type %))))
                               (remove :hidden (:notes note-data))))]
    (layout note-data
            (window (:title note-data)
                    (he/unordered-list (map note-index-item note-index))))))

(defmulti render :type)

(defmethod render :default [note-data]
  (if (= "i" (namespace (:type note-data)))
    (render-generic-index note-data)
    (render-generic note-data)))

(defmethod render :home [note-data]
  (let [notes (remove :hidden (:notes note-data))
        project-notes (filter #(= :project-note (:type %)) notes)
        learning-notes (filter #(= :learning-note (:type %)) notes)
        blog-notes (filter #(= :blog-note (:type %)) notes)]
    (layout note-data
            (list
             [:section.welcome
              (he/image {:class "avatar"} "/assets/img/avatar.png")
              [:span.name "Stel Abrego"]
              [:div.text
               [:p "Hi! I'm a freelance software engineer with a focus on functional design and web technologies."]
               [:p "Check out my projects, learning resources, and blog posts."]
               [:p "I also offer virtual tutoring for coding students. Please message me if you're interested."]]]
             (home-content-window "coding projects" "/cool-stuff-like/" project-notes)
             (home-content-window "learning resources" "/and-learns-from/" learning-notes)
             (home-content-window "blog" "/and-blogs-about/" blog-notes)))))

(defmethod render :404 [note-data]
  (layout note-data
          [:h1 "404 ;-;"]))
