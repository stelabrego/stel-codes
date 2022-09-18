# Stel's Developer Blog

The code in this repo builds my personal blog which is hosted at [stel.codes](https://stel.codes). I'm using my own Clojure library called [Nuzzle](https://github.com/stelcodes/nuzzle) to build a static site. Actually Nuzzle began as code in this repo before I decided to turn it into a standalone project.

- `site.clj`: Most of the site lives here, all the site data and hiccup functions.
- `deps.edn`: Here I define a Clojure CLI tool alias called `build` which allows me to easily call functions in `site.clj`
- `bb.edn`: Helpful task running with Babashka

## Development Workflow

![Pretty logging when running nuzzle.core/develop](https://user-images.githubusercontent.com/22163194/190880734-ff36a238-a00c-4431-a720-5308c07f57a1.png)

To develop my site, I call `clj -T:site develop`. This calls `nuzzle.core/develop` which starts dual development servers. One server is the website server which calls the `:nuzzle/render-page` function for each page I request in the browser, and the other is an nREPL server that chooses a random available port and writes an `.nrepl-port` file to the current working directory so I can quickly jump into my editor and start reloading functions in `site.clj`. I don't even need to refresh the page because I use the `:refresh-interval` option for `nuzzle.core/develop` to load some javascript that automatically refreshes the page at the given interval.

The content of the site is stored in the `content` directory in Markdown files. Every request to the development website server will get the most recent version of these files because every request triggers a new call to the page's `:nuzzle/render-page` function. I can edit the Markdown and see the website update automatically when using the `:refresh-interval` option.

I keep the static assets of the website in the `public` directory and use the `:overlay-dir` option to include them in the static site generation. I like writing CSS as SCSS, so I run `bb sass` in another terminal pane which invokes the NPM sass-compiler package `sass` to watch for changes in the `styles` directory and replace the CSS in the `public` directory when an SCSS file changes. Again, I don't even need to refresh the browser to see the changes take place when using the `:refresh-interval` option.

These techniques create an **extremely** fast feedback loop for all aspects of my static site while I'm developing. With Nuzzle I can spend more time designing and playing with things!

## Exporting the Site

When I want to publish the site, I run `clj -T:site publish` which exports the site to `dist` by default. I generate a sitemap and Atom feed by using the `:sitemap?` and `:atom-feed` keyword args for `nuzzle.core/publish`. To double check that everything looks right, I run `bb serve` which invokes [Caddy](https://github.com/caddyserver/caddy) to start a static site server. I make sure the website looks alright, and then I run `bb deploy` which invokes `rsync` to copy the static site onto a remote server where it can served from a publicly available IP address.
