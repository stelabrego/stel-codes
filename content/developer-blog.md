Every software developer needs their own website. Of course, you have already discovered mine! Thanks for visiting. 😊

This site is built using Clojure, one of my favorite programming languages. I used these Clojure libraries:

* [Stasis](https://github.com/magnars/stasis) : A collection of functions for building a static site. Basically lets you design your own static site generator.
* [Hiccup](https://github.com/weavejester/hiccup) : Allows you to write HTML using Clojure data structures. Basically blurs the boundary between code and markup, so you can use Clojure functions and libraries inside the markup seemlessly. Makes me never want to write raw HTML ever again. 😅
* [next.jdbc](https://github.com/seancorfield/next-jdbc) : Run SQL commands against a relational database. In this case, Postgres.
* [Timbre](https://github.com/ptaoussanis/timbre) : Log events. A big upgrade from println.
* [http-kit](https://github.com/http-kit/http-kit) : A minimal web server/client. I used this library to make a web server that listens for webhook triggers to rebuild the site.

Other tools I used to build and maintain this site:

* Postgres: Database in which all content is stored. Runs locally on cloud VM.
* nginx: Serves up the static HTML pages
* NixOS: Cloud VM operating system, running as a Digital Ocean Droplet.
