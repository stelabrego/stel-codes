(ns views
  (:require
   [clojure.string :as str]
   [nuzzle.hiccup :refer [raw]]))

(defn kebab-case->lower-case
  [s]
  (->> (str/split (name s) #"-")
       (map str/lower-case)
       (str/join " ")))

(defn image
  "Two-arity version is ambigious"
  ([src] (image {} src ""))
  ([opts-or-src src-or-alt]
   (if (map? opts-or-src)
     (image opts-or-src src-or-alt "")
     (image {} opts-or-src src-or-alt)))
  ([opts src alt] [:img (merge opts {:src src :alt alt})]))

(defn unordered-list
  ([items] (unordered-list {} items))
  ([opts items]
   (vec (concat [:ul opts] (for [item items] [:li item])))))

(comment (= (unordered-list (list [:p "A"] [:p "B"] [:p "C"]))
            [:ul {} [:li [:p "A"]] [:li [:p "B"]] [:li [:p "C"]]])
         (= (unordered-list {:class "foo"} (list [:p "A"] [:p "B"] [:p "C"]))
            [:ul {:class "foo"} [:li [:p "A"]] [:li [:p "B"]] [:li [:p "C"]]]))

(def os-logo-url "https://user-images.githubusercontent.com/22163194/171326988-478d1722-b895-4852-a1e4-5689c736b635.svg")

(defn header
  [{:nuzzle/keys [get-config]}]
  (let [{:keys [twitter github email]} (get-config :meta)]
    [:header
     [:nav [:a {:id "brand" :href "/"} (image os-logo-url) [:span "stel.codes"]]
      [:ul {:id "social"}
       [:li [:a {:href github} "Github"]]
       [:li [:a {:href twitter} "Twitter"]]
       [:li [:a {:href email} "Email"]]]]]))

(defn footer [] [:footer [:p "Stel Abrego 2021"]])

(defn window
  [title body]
  (let [bars (raw (slurp "svg/bars.svg"))]
    [:section.window
     [:div.top bars [:span.title title] bars]
     [:div.content body]]))

(defn tag-group
  [{:nuzzle/keys [get-config tags]}]
  {:pre [(set? tags)]}
  [:p.tags
   (for [tag tags]
     (let [{:nuzzle/keys [url title]} (get-config [:tags tag])]
       [:a {:class "tag" :href url} title]))])

(defn window-index-item
  [{:nuzzle/keys [url title tags] :keys [subtitle] :as page}]
  (list [:a {:class "title" :href url} title]
        (when subtitle [:p.subtitle subtitle])
        (when (not-empty tags) (tag-group page))))

(defn truncated-index-window
  "Expects a group index page"
  [{:nuzzle/keys [get-config index title url]}]
  (when-not (empty? index)
    (window title
            (list (->> index
                       (map get-config)
                       (sort-by :sort)
                       (reverse)
                       (take 5)
                       (map window-index-item)
                       (unordered-list {:class "index-list"}))
                  (when (> (count index) 5) [:a {:class "more-link" :href url} "more!"])))))

(defn index-window [{:nuzzle/keys [title index get-config]}]
  (window title
          (unordered-list {:class "index-list"}
                          (->> index (map get-config) (map window-index-item)))))

(defn welcome-section []
  [:section.welcome
   (image {:class "avatar"} "https://user-images.githubusercontent.com/22163194/164172131-9086a741-caa7-4811-b5b0-96e3d0f93b7f.png")
   [:span.name "Stel Abrego, Software Developer"]
   [:div.text
    [:p "Hi! I'm a freelance software hacker with a focus on functional design and web technologies."]
    [:p "Check out my projects, learning resources, and blog posts."]
    ;; TODO fix CV link or render this from markdown
    #_[:p "If you're interested in hiring me, here's my CV I also offer virtual tutoring for coding students."]]])

(defn layout
  [{:nuzzle/keys [title page-key] :as page} & content]
  [:html
   [:head
    [:title (if title (str title " | stel.codes") "stel.codes")]
    [:meta {:charset "utf-8"}]
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
    (when nil
      [:script
       {:src "https://plausible.io/js/plausible.js", :data-domain "stel.codes", :defer "defer", :async "async"}])]
   [:body (header page) [:main (when (= [] page-key) {:class "home"}) content] (footer)]])

(defn render-generic-page
  [{:nuzzle/keys [page-key title tags render-content]
    :keys [repo prod source subtitle header-image] :as page}]
  (layout page
          (welcome-section)
          (window (kebab-case->lower-case (if (> (count page-key) 1) (nth page-key (- (count page-key) 2)) (first page-key)))
                  [:article (when header-image (image header-image)) [:h1 title]
                   (when subtitle [:p.subtitle subtitle])
                   (when (not-empty tags) (tag-group page))
                   (when (or repo prod source)
                     [:div.top-links (when repo [:span "🧙 " [:a {:href repo} "Open Source Code Repo"]])
                      (when prod [:span "🌙 " [:a {:href prod} "Live App Demo"]])
                      (when source [:span "🧑‍🎓 " [:a {:href source} "Find it here!"]])])
                   (render-content)
                   [:div.circles (take 3 (repeat (raw (slurp "svg/circle.svg"))))]])))

(defn render-index-page
  [page]
  (layout page
          (welcome-section)
          (index-window page)))

(defn render-homepage
  [{:nuzzle/keys [get-config] :as page}]
  (layout page
          (welcome-section)
          (truncated-index-window (get-config [:coding-projects]))
          (truncated-index-window (get-config [:educational-media]))
          (truncated-index-window (get-config [:blog-posts]))))

(defn render-page [{:nuzzle/keys [page-key index] :as page}]
  (cond
    (= [] page-key) (render-homepage page)
    (= [:404] page-key) (layout page [:h1 "404 ;-;"])
    index (render-index-page page)
    :else (render-generic-page page)))
