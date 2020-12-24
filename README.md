stelcodes.dev-website
============================
This website is intended to promote my professional software developer career. I decided to make a static website that looked like an Mac OS 9 desktop. I'm happy with the result. I loved using Stasis to create my own static site generator. I ported this site from a Hugo project I made while I was taking classes at college. So I wanted to see if I could recreate the great parts about Hugo in Clojure while also enjoying the absolute luxury of writing html with Hiccup. I literally can't go back to writing raw html strings in templating languages anymore. I'm spoiled. 😇

I love to play with static site generators. I started using Hugo and got hooked. I wrote a blog piece about a fun Hugo experiment. You can check out my hugo projects at https://stel.codes/tags/hugo. Hopefully that works! Because that's what the code inside this repository makes possible. Adding a tag system felt like an accomplishment. I was recreating Hugo features!

Another great Hugo feature is a hot reloading browser preview. Setting this up was crucial. I need a fast workflow to develop a stylish and functional UI. Feedback loop? Is that what they call that?

This code uses Stasis, a clojure library that provides pieces of a static site generator. It gives this project a ring handler to use for development, and an export function that builds out static site in `site/`.

Usage
-----------------------------
Export the site:
```
clj -X:export
```

Start the development server:
```
clj -X:serve
```

Copyright © 2020 Stel Abrego

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
