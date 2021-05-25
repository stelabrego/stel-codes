My Developer Blog
============================
Hi! 👋 This is the code I use to build [my developer blog](https://stel.codes).

I basically use Stasis to create my own static site generator. I ported this site from a Hugo project. I wanted to recreate the great parts about Hugo in Clojure because I *love* writing html with Hiccup. I literally can't go back to writing raw html strings in templating languages anymore. Ha!

I love to play with static site generators. I made one using Webpack and js, then I used Hugo for a while. It's a relatively simple problem domain but I think it's a great way to start learning about programming for beginners.

Usage
-----------------------------
Export the site:
```
clj -X:export
```

Start the development server:
```
clj -M:repl
```
