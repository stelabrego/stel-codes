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
