(ns stelcodes.dev-blog.state
  (:require [next.jdbc.sql :as sql]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as result-set]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as string]))

(def spy #(do (println "DEBUG:" %) %))

(def db-spec
  {:dbtype "postgresql",
   :dbname "dev_blog",
   :host "127.0.0.1",
   :port 5432,
   :user "static_site_builder"})
(def db-source (jdbc/get-datasource db-spec))
(def db-conn
  (jdbc/with-options db-source
                     {:builder-fn result-set/as-unqualified-kebab-maps}))

(defn pages
  []
  {:blog-posts (sql/query db-conn ["SELECT * FROM blog_posts"]),
   :coding-projects (sql/query db-conn ["SELECT * FROM coding_projects"]),
   :educational-media (sql/query db-conn ["SELECT * FROM educational_media"])})

(defn get-raw-pages
  []
  (concat (map #(assoc % :type :blog-posts)
            (sql/query db-conn ["SELECT * FROM blog_posts"]))
          (map #(assoc % :type :coding-projects)
            (sql/query db-conn ["SELECT * FROM coding_projects"]))
          (map #(assoc % :type :educational-media)
            (sql/query db-conn ["SELECT * FROM educational_media"]))))

(defn get-raw-files [] (sql/query db-conn ["SELECT * FROM directus_files"]))

(defn get-cdn-uri-for-uuid
  [uuid]
  (->> (get-raw-files)
       (some #(when (= uuid (:id %)) %))
       (:filename-disk)
       (str "https://s3.stel.codes/")))

(comment)

(defn convert-image-uuid-to-uri
  [image-key page]
  (if-let [uuid (image-key page)]
    (assoc page image-key (get-cdn-uri-for-uuid uuid))
    page))

(defn convert-status-to-keyword
  [page]
  (if-let [status (:status page)]
    (assoc page :status (keyword status))
    page))

(defn convert-tags-to-list
  [page]
  (if-let [tags (:tags page)]
    (assoc page :tags (string/split tags #","))
    page))

(defn add-uri-to-page
  [page]
  (let [type-str (name (:type page))
        slug (:slug page)
        uri (str "/" type-str "/" slug "/")]
    (assoc page :uri uri)))

(defn published? [page] (= (:status page) :published))

(defn get-formatted-pages
  []
  (->> (get-raw-pages)
       (map convert-status-to-keyword)
       (map convert-tags-to-list)
       (map (partial convert-image-uuid-to-uri :header-image))
       (map add-uri-to-page)
       (filter published?)))

(defn kebab-case->title-case
  [s]
  (->> (string/split s #"-")
       (map string/capitalize)
       (string/join " ")))

(defn get-section-index-pages
  []
  (let [pages (get-formatted-pages)
        section-map (group-by :type pages)]
    (for [[page-type pages] section-map]
      (let [page-type-str (name page-type)
            title (kebab-case->title-case page-type-str)]
        {:title title,
         :uri (str "/" page-type-str "/"),
         :type :index,
         :indexed-pages pages}))))

(comment
  (get-section-index-pages))

(defn in? "true if coll contains elm" [coll elm] (some #(= elm %) coll))

(defn tag-name->index-uri
  [tag]
  (as-> tag $ (string/lower-case $) (csk/->kebab-case $) (str "/tags/" $ "/")))

(defn page-has-tag? [tag page] (in? (:tags page) tag))

(defn get-tag-index-pages
  []
  (let [pages (get-formatted-pages)
        tags (->> pages
                  (mapcat :tags)
                  (set))]
    (for [tag tags]
      {:title (str "#" tag),
       :type :index,
       :uri (tag-name->index-uri tag),
       :indexed-pages (filter (partial page-has-tag? tag) pages)})))

(defn get-pages
  []
  (concat (get-formatted-pages)
          (get-section-index-pages)
          (get-tag-index-pages)))

(defn get-general-information
  []
  (-> (sql/query db-conn ["SELECT * FROM general_information"])
      (first)
      (assoc :type :general-information)))

(comment
  (map convert-status-to-keyword (get-formatted-pages))
  (get-general-information)
  (get-formatted-pages)
  (get-section-index-pages)
  (get-tag-index-pages)
  (get-pages)
  (get-raw-files)
  (type (:id (first (get-raw-files))))
  (get-raw-pages))
