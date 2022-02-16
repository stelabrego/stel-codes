{:tree {
        :about
        {:resource-path "markdown/about.md"
         :title "About"}

        :blog-posts {
                     :using-directus-cms
                     {:resource-path "markdown/using-directus-cms.md",
                      :header-image "https://s3.stel.codes/d34014fb-a41f-41ed-b8ee-540a0dd35640.jpeg",
                      :sort 10,
                      :draft? false,
                      :subtitle "A flexible SQL DB interface that really shines ✨",
                      :tags [:clojure :nixos],
                      :title "Using Directus as a CMS for my Blog"}

                     :babashka-tasks-for-postgresql-backups
                     {:resource-path "markdown/babashka-postgresql-backups.md",
                      :header-image "https://s3.stel.codes/e0122757-8024-44bc-aa9b-aee8948a8b25.jpeg",
                      :sort 20,
                      :draft? false,
                      :subtitle "Why write a Bash script when you can use Clojure?!",
                      :tags [:clojure :babashka],
                      :title "Creating Babashka tasks for postgresql backups"}

                     :i3-or-sway-why-not-both
                     {:resource-path "markdown/i3-or-sway-nixos.md",
                      :header-image "https://s3.stel.codes/4f795c40-8170-4d50-be64-2e29c50d294b.png",
                      :sort 0,
                      :draft? false,
                      :subtitle "How to setup a dual i3 + sway environment on NixOS",
                      :tags [:nixos :i3 :sway :linux],
                      :title "i3 or Sway? Why not Both?"}
        }
 }
}

;; Flatter file

;; maps with :resource get a :render-resource fn
;; maps with :location get a :uri
{
 ;; Blog Posts
 :using-directus-cms
 {:resource "markdown/using-directus-cms.md",
  :location [:blog-posts]
  :header-image "https://s3.stel.codes/d34014fb-a41f-41ed-b8ee-540a0dd35640.jpeg",
  :sort 10,
  :draft? false,
  :subtitle "A flexible SQL DB interface that really shines ✨",
  :tags [:clojure :nixos],
  :title "Using Directus as a CMS for my Blog"}

 :babashka-tasks-for-postgresql-backups
 {:resource "markdown/babashka-postgresql-backups.md",
  :location [:blog-posts],
  :header-image "https://s3.stel.codes/e0122757-8024-44bc-aa9b-aee8948a8b25.jpeg",
  :sort 20,
  :draft? false,
  :subtitle "Why write a Bash script when you can use Clojure?!",
  :tags [:clojure :babashka],
  :title "Creating Babashka tasks for postgresql backups"}

 :i3-or-sway-why-not-both
 {:resource "markdown/i3-or-sway-nixos.md",
  :location [:blog-posts],
  :header-image "https://s3.stel.codes/4f795c40-8170-4d50-be64-2e29c50d294b.png",
  :sort 0,
  :draft? false,
  :subtitle "How to setup a dual i3 + sway environment on NixOS",
  :tags [:nixos :i3 :sway :linux],
  :title "i3 or Sway? Why not Both?"}

 :introduction
 {:resource "markdown/introduction.md"}
 ;; Could add :resource-type but we can infer based on filename

 :meta
 {:domain "stel.codes",
  :email "stel@stel.codes",
  :github "https://github.com/stelcodes",
  :twitter "https://twitter.com/stelstuff"}
 }
