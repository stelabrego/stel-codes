# Stel's Developer Blog

The code in this repo builds my personal blog which is hosted at [stel.codes](https://stel.codes).

I'm using my own Clojure library called [Nuzzle](https://github.com/stelcodes/nuzzle) to build a static site. Actually Nuzzle began as code in this repo before I decided to turn it into a proper library.

This repo serves as a comprehensive example of how to use Nuzzle. These files do all the heavy-lifting:
- `nuzzle.edn`: Nuzzle config
- `src/views.clj`: Hiccup-generating functions
- `src/user.clj`: REPL entrypoint
- `deps.edn`: Clojure configuration (dependencies, aliases, etc)
- `bb.edn`: Helpful task running with Babashka

## Development Workflow

To develop my site, I start an nREPL with `bb repl` which invokes `clj -M:repl` which starts Clojure using the `:repl` alias in `deps.edn`. This starts an nREPL and loads `src/user.clj` which requires the `nuzzle.api` namespace and defines a few handy functions. Once I'm in the REPL, I start the Nuzzle development server with `(start)`. After that, I can edit `nuzzle.edn` and the next response from the server will reflect the changes. I redefine the Hiccup-generating functions via nREPL with my favorite nREPL editor client [Conjure](https://github.com/olical/conjure), and the new functions are used on the next page load. To hot-reload the CSS as well, I run `bb sass` in another terminal pane which invokes the NPM sass-compiler package `sass`. It watches for changes in the `scss` directory and recompiles the CSS whenever something changes.

These techniques create an **extremely** fast feedback loop for all aspects of my static site while I'm developing.

## Exporting the Site

When I want to export the site, I run `(export)` in the REPL which is defined in `src/user.clj`. To double check that everything looks right, I run `bb serve` which invokes [Caddy](https://github.com/caddyserver/caddy) to start a static site server. I make sure the website looks alright, and then I run `bb deploy` which invokes `rsync` to copy the static site onto a remote server where it can served from a publicly available IP address.
