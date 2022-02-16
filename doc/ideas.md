It would be interesting to try to build a function that can generate shortcode extensions for clj-markdown. I want to emulate Hugo because they got so many things right. I need shortcode support. I think clj-markdown could easily support shortcodes due to the simple nature of the parser. I could just check for `^{{` or whatever the shortcode syntax is at the beginning of the line. I would probably have to ensure the line wasn't part of a paragraph, code block, or whatever else but I believe clj-markdown includes that information in the state map.

Hmm if all the id's are unique then why not just have the id be the key in a global map. Why separate the articles from the metadata. I could just have one key called metadata.
```
{
:blah-blah {:resource "markdown/blah-blah.md"
            :category :blog-posts}

:introduction {:resource "markdown/introduction"}

:metadata {:twitter "..."}
}
```

Or
```
{
:articles [
  {
  :id :blah-blah
  :category :blog-posts
  }
]

:snippets [
  {:id :introduction
   :resource "markdown/introduction.md"}
]

:metadata {:twitter "..."}
}
```

What if I made a cool metaphor between pages/partials like stone/shard article/snippet. Also work "realized-site" in there earth/stone/shard or world/stone/shard. Or 

Some more reorganization ideas:
These functions could be attached to every page:
```
get-article get-snippet get-tag get-category
```
or if I enforce uniqueness across all articles, snippets, tags, and categories. Categories and articles already need to be unique.

```
{:articles [
            ;; Blog Posts
            {:resource-path "markdown/using-directus-cms.md",
             :category :blog-posts,
             :header-image "https://s3.stel.codes/d34014fb-a41f-41ed-b8ee-540a0dd35640.jpeg",
             :id :using-directus-cms
             :sort 10,
             :draft? false,
             :subtitle "A flexible SQL DB interface that really shines ✨",
             :tags [:clojure :nixos],
             :title "Using Directus as a CMS for my Blog"}

            {:resource-path "markdown/babashka-postgresql-backups.md",
             :category :blog-posts,
             :header-image "https://s3.stel.codes/e0122757-8024-44bc-aa9b-aee8948a8b25.jpeg",
             :id :babashka-tasks-for-postgresql-backups
             :sort 20,
             :draft? false,
             :subtitle "Why write a Bash script when you can use Clojure?!",
             :tags [:clojure :babashka],
             :title "Creating Babashka tasks for postgresql backups"}

            {:resource-path "markdown/i3-or-sway-nixos.md",
             :category :blog-posts,
             :header-image "https://s3.stel.codes/4f795c40-8170-4d50-be64-2e29c50d294b.png",
             :id :i3-or-sway-why-not-both
             :sort 0,
             :draft? false,
             :subtitle "How to setup a dual i3 + sway environment on NixOS",
             :tags [:nixos :i3 :sway :linux],
             :title "i3 or Sway? Why not Both?"}]}

;; Or this:

{
;; Blog Posts
:using-directus-cms
{:resource-path "markdown/using-directus-cms.md",
 :category :blog-posts,
 :header-image "https://s3.stel.codes/d34014fb-a41f-41ed-b8ee-540a0dd35640.jpeg",
 :sort 10,
 :draft? false,
 :subtitle "A flexible SQL DB interface that really shines ✨",
 :tags [:clojure :nixos],
 :title "Using Directus as a CMS for my Blog"}

:babashka-tasks-for-postgresql-backups 
{:resource-path "markdown/babashka-postgresql-backups.md",
 :category :blog-posts,
 :header-image "https://s3.stel.codes/e0122757-8024-44bc-aa9b-aee8948a8b25.jpeg",
 :sort 20,
 :draft? false,
 :subtitle "Why write a Bash script when you can use Clojure?!",
 :tags [:clojure :babashka],
 :title "Creating Babashka tasks for postgresql backups"}

:i3-or-sway-why-not-both
{:resource-path "markdown/i3-or-sway-nixos.md",
 :category :blog-posts,
 :header-image "https://s3.stel.codes/4f795c40-8170-4d50-be64-2e29c50d294b.png",
 :sort 0,
 :draft? false,
 :subtitle "How to setup a dual i3 + sway environment on NixOS",
 :tags [:nixos :i3 :sway :linux],
 :title "i3 or Sway? Why not Both?"}

:_meta_
{:domain "stel.codes",
 :email "stel@stel.codes",
 :github "https://github.com/stelcodes",
 :twitter "https://twitter.com/stelstuff"}
}

;; Or this:

{
;; Blog Posts
:using-directus-cms
{:resource-path "markdown/using-directus-cms.md",
 :page-location [:blog-posts]
 :header-image "https://s3.stel.codes/d34014fb-a41f-41ed-b8ee-540a0dd35640.jpeg",
 :sort 10,
 :draft? false,
 :subtitle "A flexible SQL DB interface that really shines ✨",
 :tags [:clojure :nixos],
 :title "Using Directus as a CMS for my Blog"}

:babashka-tasks-for-postgresql-backups 
{:resource-path "markdown/babashka-postgresql-backups.md",
 :page-location [:blog-posts],
 :header-image "https://s3.stel.codes/e0122757-8024-44bc-aa9b-aee8948a8b25.jpeg",
 :sort 20,
 :draft? false,
 :subtitle "Why write a Bash script when you can use Clojure?!",
 :tags [:clojure :babashka],
 :title "Creating Babashka tasks for postgresql backups"}

:i3-or-sway-why-not-both
{:resource-path "markdown/i3-or-sway-nixos.md",
 :page-location [:blog-posts],
 :header-image "https://s3.stel.codes/4f795c40-8170-4d50-be64-2e29c50d294b.png",
 :sort 0,
 :draft? false,
 :subtitle "How to setup a dual i3 + sway environment on NixOS",
 :tags [:nixos :i3 :sway :linux],
 :title "i3 or Sway? Why not Both?"}

:introduction
{:resource-path "markdown/introduction.md"}
;; Could add :resource-type but we can infer based on filename

:_meta_
{:domain "stel.codes",
 :email "stel@stel.codes",
 :github "https://github.com/stelcodes",
 :twitter "https://twitter.com/stelstuff"}
}

```
or
```clojure
{
  :tree {
   :about {
     :resource-path "markdown/about.md"
     :title "About"
   }
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
```

The `:__meta` key could just be a separate file.

