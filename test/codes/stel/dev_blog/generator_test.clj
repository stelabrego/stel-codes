(ns codes.stel.dev-blog.generator-test
  (:require [clojure.test :refer [deftest is run-tests]]
            [codes.stel.dev-blog.generator :as generator]))

(deftest create-tag-index
  (is (= {[:tags :bar] {:index [[:blog :foo]], :title "#bar", :uri "/tags/bar/"},
          [:tags :baz] {:index [[:blog :foo]], :title "#baz", :uri "/tags/baz/"}}
         (generator/create-tag-index {[:blog :foo] {:tags [:bar :baz]} [:about] {} }) )))

(deftest id->uri
  (is (= "/blog-posts/my-hobbies/" (generator/id->uri [:blog-posts :my-hobbies])))
  (is (= "/about/" (generator/id->uri [:about]))))

(deftest create-group-index
  (is (= (generator/create-group-index {[:blog :foo] {:title "Foo"} [:blog :archive :baz] {:title "Baz"} [:projects :bee] {:title "Bee"}})
         {[:blog]
          {:index [[:blog :foo]], :title "Blog", :uri "/blog/"},
          [:blog :archive]
          {:index [[:blog :archive :baz]],
           :title "Archive",
           :uri "/blog/archive/"},
          [:projects]
          {:index [[:projects :bee]], :title "Projects", :uri "/projects/"}})))

(comment (run-tests))
