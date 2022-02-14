(ns codes.stel.dev-blog.views
  (:require [hiccup2.core :refer [html raw]]
            [clojure.java.io :as io]
            [codes.stel.dev-blog.config :refer [config]]
            [codes.stel.dev-blog.state :as state]))

(defn image
  "Two-arity version is ambigious"
  ([src] (image {} src ""))
  ([opts-or-src src-or-alt]
   (if (map? opts-or-src)
     (image opts-or-src src-or-alt "")
     (image {} opts-or-src src-or-alt)
     ))
  ([opts src alt] [:img (merge opts {:src src :alt alt})]))

(defn unordered-list
  ([items] (unordered-list {} items))
  ([opts items]
   [:ul opts (for [item items] [:li item])]))

(defn header
  [{:keys [general]}]
  (let [{:keys [twitter github email]} general]
    [:header
     [:nav [:a {:id "brand" :href "/"} (image "https://s3.stel.codes/nixos-logo.png") [:span "stel.codes"]]
      [:ul {:id "social"}
       [:li [:a {:href github} "Github"]]
       [:li [:a {:href twitter} "Twitter"]]
       [:li [:a {:href email} "Email"]]]]]))

(defn footer [] [:footer [:p "Stel Abrego 2021"]])

(defn window
  [title body]
  (let [bars (raw (slurp (io/resource "svg/bars.svg")))]
    [:section.window
     [:div.top bars [:span.title title] bars]
     [:div.content body]]))

(defn tag-group
  [{:keys [id->info]} tags]
  [:p.tags (for [tag tags]
             (list "#" [:a {:class "tag" :href (-> (id->info tag) :uri)} tag]))])

(defn window-list-item
  [realized-site {:keys [uri title subtitle tags] :as _article}]
  (list [:a {:class "title" :href uri} title]
        (when subtitle [:p.subtitle subtitle])
        (when (not-empty tags) (tag-group realized-site tags))))

(defn home-content-window
  [{:keys [id->info] :as realized-site} category-id]
  (let [{:keys [indexed-articles title uri]} (id->info category-id)]
    (when-not (empty? indexed-articles)
      (window title
              (list (->> indexed-articles
                         id->info
                         (sort-by :sort)
                         (reverse)
                         (take 5)
                         (map (partial window-list-item realized-site))
                         (unordered-list))
                    (when (> (count indexed-articles) 5) [:a {:class "more-link" :href uri} "more!"]))))))

(defn welcome-section
  []
  [:section.welcome (image {:class "avatar"} "https://s3.stel.codes/avatar-small.png")
   [:span.name "Stel Abrego, Software Developer"]
   [:p "Hi! I'm a freelance software hacker with a focus on functional design and web technologies."]
   [:p "Check out my projects, learning resources, and blog posts."]
   ;; TODO fix CV link or render this from markdown
   [:p "If you're interested in hiring me, here's my CV I also offer virtual tutoring for coding students."]])

(defn layout
  [realized-site {:keys [title category] :as page} & content]
  (-> (html
        {:lang "en"}
        [:head [:title (if title (str title " | stel.codes") "stel.codes")] [:meta {:charset "utf-8"}]
         [:meta {:http-equiv "X-UA-Compatible", :content "IE=edge"}]
         [:meta {:name "viewport", :content "width=device-width, initial-scale=1.0"}]
         ;; Icons
         [:link {:href "/assets/icons/apple-touch-icon.png", :sizes "180x180", :rel "apple-touch-icon"}]
         [:link {:href "/assets/icons/favicon-32x32.png", :sizes "32x32", :type "image/png", :rel "icon"}]
         [:link {:href "/assets/icons/favicon-16x16.png", :sizes "16x16", :type "image/png", :rel "icon"}]
         [:link {:href "/assets/icons/site.webmanifest", :rel "manifest"}]
         [:link {:color "#5bbad5", :href "/assets/icons/safari-pinned-tab.svg", :rel "mask-icon"}]
         [:link {:href "/assets/icons/favicon.ico", :rel "shortcut icon"}]
         [:link {:href "/assets/css/main.css" :rel "stylesheet"}]
         [:meta {:content "#da532c", :name "msapplication-TileColor"}]
         [:meta {:content "/assets/icons/browserconfig.xml", :name "msapplication-config"}]
         [:meta {:content "#ffffff", :name "theme-color"}]
         ;; Analytics
         (when (:prod config)
           [:script
            {:src "https://plausible.io/js/plausible.js", :data-domain "stel.codes", :defer "defer", :async "async"}])]
        [:body (header realized-site) [:main {:class (name category)} content] (footer)])
      (str)))

(defn render-generic
  [realized-site {:keys [repo prod source category title subtitle tags header-image render-resource] :as page}]
  (layout page
          (welcome-section)
          (window (state/kebab-case->lower-case (name category))
                  [:article (when header-image (image header-image)) [:h1 title]
                   (when subtitle [:p.subtitle subtitle])
                   (when (not-empty tags) (tag-group realized-site tags))
                   (when (or repo prod source)
                     [:div.top-links (when repo [:span "🧙 " [:a {:href repo} "Open Source Code Repo"]])
                      (when prod [:span "🌙 " [:a {:href prod} "Live App Demo"]])
                      (when source [:span "🧑‍🎓 " [:a {:href source} "Find it here!"]])])
                   (raw (render-resource))
                   [:div.circles (take 3 (repeat (raw (slurp "resources/svg/circle.svg"))))]])))

(defn render-generic-index
  [realized-site {:keys [title indexed-articles] :as page}]
  (layout
   realized-site
   page
   (list (welcome-section)
         (window title
                 (unordered-list
                  (map (partial window-list-item realized-site) indexed-articles))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Rendering multimethod declarations

(defmulti render :category)

(defmethod render :default [realized-site page] (render-generic realized-site page))

(defmethod render :index [realized-site page] (render-generic-index realized-site page))

(defmethod render :home
  [realized-site page]
  (layout
   realized-site
   page
   (list (welcome-section)
         (home-content-window realized-site :coding-projects)
         (home-content-window realized-site :educational-media)
         (home-content-window realized-site :blog-posts))))

(defmethod render :404 [realized-site page] (layout realized-site page [:h1 "404 ;-;"]))
