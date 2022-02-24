# Stel's Developer Blog
The code in this repo builds my personal software development blog which is hosted at [stel.codes](https://stel.codes).

I'm using my own Clojure library called [Nuzzle](https://github.com/stelcodes/nuzzle) to build a static site. Actually Nuzzle began as code in this repo before I decided to turn it into a proper library.

This repo serves as a comprehensive example of how to use Nuzzle. These three files do all the heavy-lifting:
- `src/codes/stel/dev_blog/views.clj`: where all of the Hiccup-generating functions live.
- `resources/edn/site.edn`: where the site configuration lives.
- `dev/user.clj`: where the REPL workflow begins. Helper functions `start` and `export` are defined here.

## Development Workflow

To develop my site, I start an nREPL with `clj -M:repl` (see the `:repl` alias in `deps.edn`). The alias adds the `dev` directory to the classpath which makes `dev/user.clj` automatically load when the REPL starts. Then I start the Nuzzle development server with `(start)`. After that, I can edit `resources/edn/site.edn` and the next response from the server will reflect the changes. I can also redefine the Hiccup-generating functions in the REPL (I do this via the Neovim plugin [Conjure](https://github.com/olical/conjure)) and the changes will also be reflected on the next response. This creates an extremely fast feedback loop.

The only time I need to restart the development server is when I want to change the global config map passed into Nuzzle which almost never changes. Usually I will just restart the REPL. However, a REPL restart can be avoided by saving the result of `(start)` to a var such as `(def stop (start))`. `start` returns what `codes.stel.nuzzle.api/start-server` returns, which is a function that stops the development server. This way, I can call `(stop)` to stop the server, redefine the `config` var via Conjure, and then start the server again with `(def stop (start))`.

## Exporting the Site

When I want to export the site, I run `(export)` in the REPL. Then I use [Caddy](https://github.com/caddyserver/caddy) to start a static site server via `caddy file-server --listen :3030 --root dist`. The port number can be anything you like as long as it's not in use. I make sure the website looks alright, and then I can use `scp` to put the website onto a remote server.
