## Overview

I often find myself browsing [Hacker News](https://news.ycombinator.com) for updates in the tech world. I enjoy reading about open-source projects and finding blog posts that dive into technical aspects of the internet, hardware, or any kind of tech related tinkering.

In early 2020, an article on Hacker News introduced me to Clojure. From there I read Russ Olsen's excellent book [Getting Clojure](https://pragprog.com/titles/roclojure/getting-clojure), and I started watching Rich Hickey's keynote talks on YouTube. Maybe it's not suprising that I decided to make a clone of Hacker News as my first real Clojure project. The result is Functional News!

The idea of Functional News (AKA λn) is to be a clone of Hacker News but particularly for news about functional programming languages. It's definitely **not** a feauture complete copy. Not yet. A feature comparison table can be found in [the README](https://github.com/stelcodes/functional-news). I plan to keep working on it until I get it pretty close.

## Tools

For this project, I used:

- **[Clojure](https://clojure.org)** : The lanaguage the web app is written in
- **[PostgreSQL](https://postgresql.org)** : The web app speaks with a local PostgreSQL database and saves app state here
- **[NixOS](https://nixos.org)** : The app and database are hosted on a cloud VM running NixOS, my favorite Linux distribution and one that I've [contributed to](https://git.io/JWADA)!

Some Clojure libraries I used:
- **[http-kit](https://github.com/http-kit/http-kit)** : A minimal web server/client. The web app uses http-kit as a web server.
- **[Reitit](https://github.com/metosin/reitit)** : A data-driven routing library that has with lots of handy features like parameter validation.
- **[Hiccup](https://github.com/weavejester/hiccup)** : HTML templating as Clojure data structures.
- **[next.jdbc](https://github.com/seancorfield/next-jdbc)** : Allows you to connect to a relational database and execute SQL commands. In this case, a local PostgreSQL database.
- **[Timbre](https://github.com/ptaoussanis/timbre)** : Log events.

## Architectural Overview

Since this app is written in Clojure, I'll provide an overview about each namespace:

### [codes.stel.functional-news.config](https://github.com/stelcodes/functional-news/blob/main/src/codes/stel/functional_news/config.clj)

- Loads config values via [cprop](https://github.com/tolitius/cprop).

### [codes.stel.functional-news.core](https://github.com/stelcodes/functional-news/blob/main/src/codes/stel/functional_news/core.clj)

- Provides a `-main` function to run from uberjar, imports `codes.stel.functional-news.http` to start web app server

### [codes.stel.functional-news.handler](https://github.com/stelcodes/functional-news/blob/main/src/codes/stel/functional_news/handler.clj)

- Defines helper functions for creating HTTP response maps
- Defines handlers for all GET endpoints which use `codes.stel.functional-news.views` functions to generate HTML.
- Defines handlers for all POST endpoints
- Defines [Sieppari](https://github.com/metosin/sieppari) interceptors for session creation, exception handling, and logging
- Defines a Reitit app to pass to http-kit

### [codes.stel.functional-news.http](https://github.com/stelcodes/functional-news/blob/main/src/codes/stel/functional_news/http.clj)

- Starts an http-kit server with app function from `codes.stel.functional-news.handler`.

### [codes.stel.functional-news.state](https://github.com/stelcodes/functional-news/blob/main/src/codes/stel/functional_news/state.clj)

- Defines a connection to the local PostgreSQL database running on the server
- Defines a function for every SQL command used by the app such as `find-user`, `create-user`, `find-submission`, `create-comment`, and `create-upvote`.

### [codes.stel.functional-news.views](https://github.com/stelcodes/functional-news/blob/main/src/codes/stel/functional_news/views.clj)

- Defines functions that render components of the web app such as `header`, `nav`, `submission-list`, `submission-list-item`, and `footer`.
- Defines fnctions that render whole pages of the web app which compose the aforementioned functions to render a complete HTML document. Function names include `login-page`, `submit-page`, and `not-found`.

### [codes.stel.functional-news.util](https://github.com/stelcodes/functional-news/blob/main/src/codes/stel/functional_news/util.clj)

- Defines a function to generate usernames that combine functional programming jargon with animal names. This creates usernames such as "IsomorphicOtter" and "VariadicFlamingo". I'm very proud of this one. 😄
- Defines validation functions like `validate-email`, `validate-url`, and `validate-password`.

## Improving Hacker News

Sadly, my least favorite part of Hacker News is the culture. In fact, I often find non-technical Hacker News sumbissions with comments that justify abhorrent violence against marginalized people. For example, take a gander at [all the people](https://news.ycombinator.com/item?id=27987358) defending a mining company that destroys indigenous archeological sites in Australia. I've almost stopped reading comments altogether. As a queer person, these things affect me. When I was getting a comp sci degree I constantly had to deal with rude, transphobic professors.

On the contrary, one of my favorite aspects of the Clojure community is how welcoming it is. I want Functional News to be a welcoming space too. One way we can encourage marginalized people to code is to build safe spaces for people to learn and share. I'm not sure if anyone will use Functional News, but if it ever takes off, one of my goals would be to improve Hacker New's community guidelines and moderation.u
