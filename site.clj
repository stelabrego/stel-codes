(ns site
  (:require
   [clojure.string :as str]
   [nuzzle.api :as nuzz]
   [nuzzle.hiccup :as hiccup]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reused static values

(def base-url "https://stel.codes")

(def social {:email "stel@stel.codes"
             :github "https://github.com/stelcodes"
             :twitter "https://twitter.com/stelstuff"})

(def os-logo-url "https://user-images.githubusercontent.com/22163194/171326988-478d1722-b895-4852-a1e4-5689c736b635.svg")

(def stel {:name "Stel Abrego" :email "stel@stel.codes" :url "https://stel.codes"})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils

(defn kebab-case->lower-case
  [s]
  (->> (str/split (name s) #"-")
       (map str/lower-case)
       (str/join " ")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Views

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

(defn header []
  (let [{:keys [twitter github email]} social]
    [:header
     [:nav [:a {:id "brand" :href "/"} (image os-logo-url) [:span "stel.codes"]]
      [:ul {:id "social"}
       [:li [:a {:href github} "Github"]]
       [:li [:a {:href twitter} "Twitter"]]
       [:li [:a {:href email} "Email"]]]]]))

(defn footer [] [:footer [:p "Stel Abrego 2021"]])

(defn window
  [title body]
  (let [bars (hiccup/raw-html (slurp "images/bars.svg"))]
    [:section.window
     [:div.top bars [:span.title title] bars]
     [:div.content body]]))

(defn tag-group
  [{:nuzzle/keys [tags] :as _page}]
  (when tags
    [:p.tags
     (for [tag tags]
       [:a {:class "tag" :href [:tags tag]} (str "#" (name tag))])]))

(defn window-index-item
  [{:nuzzle/keys [url title tags] :keys [subtitle] :as page}]
  (list [:a {:class "title" :href url} title]
        (when subtitle [:p.subtitle subtitle])
        (when (not-empty tags) (tag-group page))))

(defn truncated-index-window
  "Expects a group index page"
  [{:nuzzle/keys [get-pages title url]}]
  (when-let [children-pages (get-pages url :children? true)]
    (window title
            (list (->> children-pages
                       (sort-by :sort)
                       (reverse)
                       (take 5)
                       (map window-index-item)
                       (unordered-list {:class "index-list"}))
                  (when (> (count children-pages) 5) [:a {:class "more-link" :href url} "more!"])))))

(defn index-window [{:nuzzle/keys [title index get-pages]}]
  (window title
          (unordered-list {:class "index-list"}
                          (->> index (map get-pages) (map window-index-item)))))

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
  [{:nuzzle/keys [title url] :as _page} & content]
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
   [:body
    (header)
    [:main (when (= [] url) {:class "home"}) content]
    (footer)]])

(defn render-generic-page
  [{:nuzzle/keys [url title tags render-content]
    :keys [repo prod source subtitle header-image] :as page}]
  (layout page
          (welcome-section)
          (window (kebab-case->lower-case (first url))
                  [:article (when header-image (image header-image)) [:h1 title]
                   (when subtitle [:p.subtitle subtitle])
                   (when (not-empty tags) (tag-group page))
                   (when (or repo prod source)
                     [:div.top-links (when repo [:span "🧙 " [:a {:href repo} "Open Source Code Repo"]])
                      (when prod [:span "🌙 " [:a {:href prod} "Live App Demo"]])
                      (when source [:span "🧑‍🎓 " [:a {:href source} "Find it here!"]])])
                   (render-content)
                   [:div.circles (take 3 (repeat (hiccup/raw-html (slurp "images/circle.svg"))))]])))

(defn render-index-page
  [page]
  (layout page
          (welcome-section)
          (index-window page)))

(defn render-homepage
  [{:nuzzle/keys [get-pages] :as page}]
  (layout page
          (welcome-section)
          (truncated-index-window (get-pages [:coding-projects]))
          (truncated-index-window (get-pages [:educational-media]))
          (truncated-index-window (get-pages [:blog-posts]))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Content

(def hiccup-transformations
  {:code #(hiccup/highlight-code % :chroma {:style :dracula})})

(defn md-content [md-path]
  (fn [_page] (-> md-path
                  slurp
                  nuzz/parse-md
                  (hiccup/transform-hiccup hiccup-transformations))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Pages

(defn pages []
  (-> {;; Homepage
       []
       {:nuzzle/title "Home"
        :nuzzle/render-page render-homepage}

       ;; Blog Posts
       [:blog-posts]
       {:nuzzle/title "Blog Posts"
        :nuzzle/render-page render-index-page}

       [:blog-posts :using-directus-cms]
       {:nuzzle/title "Using Directus as a CMS for my Blog"
        :nuzzle/render-page render-generic-page
        :nuzzle/render-content (md-content "content/using-directus-cms.md")
        :nuzzle/tags #{:clojure :nixos}
        :nuzzle/feed? true
        :nuzzle/author stel
        :header-image "https://user-images.githubusercontent.com/22163194/171312351-c9d6c835-f94b-45a4-8b8a-096bcdcf2084.jpeg"
        :sort 10
        :subtitle "A flexible SQL DB interface that really shines ✨"}

       [:blog-posts :babashka-tasks-for-postgresql-backups]
       {:nuzzle/title "Creating Babashka tasks for postgresql backups"
        :nuzzle/render-page render-generic-page
        :nuzzle/render-content (md-content "content/babashka-postgresql-backups.md")
        :nuzzle/tags #{:clojure :babashka}
        :nuzzle/feed? true
        :nuzzle/author stel
        :header-image "https://user-images.githubusercontent.com/22163194/171311927-dae60651-bedb-4a35-82f3-e4c48a8d6661.svg"
        :sort 20
        :subtitle "Why write a Bash script when you can use Clojure?!"}

       [:blog-posts :i3-or-sway-why-not-both]
       {:nuzzle/title "i3 or Sway? Why not Both?"
        :nuzzle/render-page render-generic-page
        :nuzzle/render-content (md-content "content/i3-or-sway-nixos.md")
        :nuzzle/tags #{:nixos :i3 :sway :linux}
        :nuzzle/feed? true
        :nuzzle/author stel
        :header-image "https://user-images.githubusercontent.com/22163194/171310246-ac140e63-0eff-4e19-9837-3263649512a0.png"
        :sort 0
        :subtitle "How to setup a dual i3 + sway environment on NixOS"}

       ;; Coding Projects
       [:coding-projects]
       {:nuzzle/title "Coding Projects"
        :nuzzle/render-page render-index-page}

       [:coding-projects :functional-news]
       {:nuzzle/title "Functional News (λn)"
        :nuzzle/render-page render-generic-page
        :nuzzle/render-content (md-content "content/functional-news.md")
        :nuzzle/author stel
        :nuzzle/tags #{:clojure :java :scss}
        :header-image "https://user-images.githubusercontent.com/22163194/171310275-06701d92-9d90-44a5-88c1-3a7ff25c534f.jpeg"
        :prod "https://news.stel.codes"
        :repo "https://github.com/stelcodes/functional-news"
        :sort 20}

       [:coding-projects :developer-blog]
       {:nuzzle/title "Developer Blog"
        :nuzzle/render-page render-generic-page
        :nuzzle/render-content (md-content "content/developer-blog.md")
        :nuzzle/author stel
        :nuzzle/tags #{:clojure :java :scss}
        :prod "https://stel.codes"
        :repo "https://github.com/stelcodes/dev-blog"
        :sort 10}

       [:coding-projects :self-care-android-app]
       {:nuzzle/title "Self Care Android App"
        :nuzzle/render-page render-generic-page
        :nuzzle/render-content (md-content "content/self-care-android-app.md")
        :nuzzle/tags #{:java :android}
        :header-image "https://user-images.githubusercontent.com/22163194/171310260-aefcd541-54c3-4269-b643-2af7f50337e2.gif"
        :prod nil
        :repo "https://github.com/stelcodes/self-care-android-app"
        :sort 0}

       ;; Educational Resources
       [:educational-media]
       {:nuzzle/title "Educational Media"
        :nuzzle/render-page render-index-page}

       [:educational-media :getting-clojure]
       {:nuzzle/title "Getting Clojure by Russ Olsen"
        :nuzzle/render-page render-generic-page
        :nuzzle/render-content (md-content "content/getting-clojure-russ-olsen.md")
        :nuzzle/tags #{:clojure :java}
        :header-image "https://user-images.githubusercontent.com/22163194/171310185-dd9b6cc6-d140-4873-8665-abd540110efc.jpeg"
        :sort 20
        :subtitle "A truly excellent introduction to Clojure"}

       [:educational-media :grid-layout-in-css]
       {:nuzzle/title "Grid Layout in CSS by Eric Meyer"
        :nuzzle/render-page render-generic-page
        :nuzzle/render-content (md-content "content/grid-layout-css-eric-meyer.md")
        :nuzzle/tags #{:css :scss}
        :header-image "https://user-images.githubusercontent.com/22163194/171310232-4e09ec6e-0b18-4827-bddf-21d1b52ffea7.jpeg"
        :sort 10
        :source "https://www.oreilly.com/library/view/grid-layout-in/9781491930205"
        :subtitle "A great reference for the ultimate CSS layout module"}}
      (nuzz/add-tag-pages render-index-page)))

(comment (pages))

(defn develop [_]
  (nuzz/serve #'pages :overlay-dir "public"))

(defn publish [_]
  (nuzz/publish pages :atom-feed {:title "Stel Codes"} :overlay-dir "public"))
