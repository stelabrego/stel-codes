{
:title "Literary Magazine Concept Website"
:location :coding-journal
:date "2019-09-15"
:repo "literary-magazine-concept"
:tags ["javascript" "css" "markdown" "css"]
}

<img src='https://media.giphy.com/media/YN8SoLCspZBEUc7k0y/giphy.gif' class='mobile-phone'>

Inspired by online literary magazines like [Butter Press](https://www.bttrprss.com), I created my own literary magazine website called *[My Delicate Sun](https://literary-magazine-concept.stelabrego.com/)*. The fictional magazine's mission is to publish works of LGBTQIA+ artists. It's built with `go` templating and Hugo, the static site generator. It features custom SVG's, fancy CSS backgrounds, and a fully integrated [forestry.io](https://forestry.io) content management system which makes adding and updating content incredibly easy for a non-technical editor.

# Website Structure

I decided *My Delicate Sun* publishes two types of content for their readers. These two data types are:

- **Issues:** collections of artwork and commentary
- **Artist Spotlights:** articles about up-and-coming artists with a photo of the artist and some of their artwork afterwords.

Internally, the **Issue** and **Spotlight** data types reference two other data types: 

- **Artists:** Name and photo
- **Artworks:** Title, artist name, and some literary content (like a poem)

This way, the editor doesn't have to replicate data between spotlights and issues.

# Example Artist Markdown

```md
#site/content/artist/emery-davis.md

+++
image = "/media/emery-davis.jpg"
title = "Emery Davis"
+++

```

# Example Artwork Markdown

```
#site/content/artwork/flowers.md

+++
artist = "artists/emery-davis.md"
title = "Flowers"
+++

Whose flower is that? I think I know.  
Its owner is quite angry though.  
She was cross like a dark potato.  
I watch her pace. I cry hello.

She gives her flower a shake,  
And screams I've made a bad mistake.  
The only other sound's the break,  
Of distant waves and birds awake.

The flower is kind, gentle and deep,  
But she has promises to keep,  
Tormented with nightmares she never sleeps.  
Revenge is a promise a girl should keep.

She rises from her cursed bed,  
With thoughts of violence in her head,  
A flash of rage and she sees red.  
Without a pause I turned and fled.

```

# Example Issue Markdown

```md
#site/content/issues/summer-2019.md

+++
date = "2019-07-09T22:43:55+00:00"
featuredArt = ["artwork/flowers.md", "artwork/warmth-of-literature.md"]
title = "Summer 2019"
+++

```
# Example Spotlight Markdown

```md
#site/content/spotlight/the-tender-art-of-emery.md

+++
artist = "artists/emery-davis.md"
date = "2019-09-18T23:52:03+00:00"
featuredArt = ["artwork/flowers.md"]
title = "The Tender Art of Emery Davis"
+++

Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer nec odio. Praesent libero. Sed cursus ante dapibus diam. Sed nisi. Nulla quis sem at nibh elementum imperdiet. Duis sagittis ipsum. Praesent mauris. Fusce nec tellus sed augue semper porta. Mauris massa. Vestibulum lacinia arcu eget nulla. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur sodales ligula in libero. Sed dignissim lacinia nunc. 

Sed nisi. Nulla quis sem at nibh elementum imperdiet. Duis sagittis ipsum. Praesent mauris. Fusce nec tellus sed augue semper porta. Mauris massa. Vestibulum lacinia arcu eget nulla. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur sodales ligula in libero. Sed dignissim lacinia nunc. 

Mauris massa. Vestibulum lacinia arcu eget nulla. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Curabitur sodales ligula in libero. Sed dignissim lacinia nunc. 
```

# Forestry.io Integration

Once I implemented the website structure, I wanted to practice integrating it into a CMS which could be used by a non-technical editor.

![website](/img/my-delicate-sun-cms.jpg)

[Forestry.io](https://forestry.io) was the perfect choice because they support Hugo static site builds (as well as Jekyll, Gatsby, and others). A cool feature of their UI is the ability to reference other documents within the project. For example, when an editor creates a new **Spotlight** piece, the **Artist** field is displayed as a drop down list which displays the titles of the **Artist** markdown files which exist in the project. The editor chooses one of them, and then their selection is saved as the path to that markdown file. Neat!

![website](/img/my-delicate-sun-cms-2.jpg)
