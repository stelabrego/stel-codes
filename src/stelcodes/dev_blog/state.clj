(ns stelcodes.dev-blog.state
  (:require [next.jdbc.sql :as sql]
            [next.jdbc :as jdbc]))

(def db-spec {:dbtype "postgresql" :dbname "dev_blog" :host "127.0.0.1" :port 5432 :user "static_site_builder"})
(def db-conn (jdbc/get-datasource db-spec))

(sql/query db-conn ["SELECT * FROM blog_posts"])
