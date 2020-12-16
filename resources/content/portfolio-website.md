{
:title "Old Portfolio Website"
:location :coding-journal
:date "2019-02-27"
:repo "portfolio"
:tags ["go" "javascript" "css"]
}

<img class='mobile-phone' src='https://media.giphy.com/media/TiO9awohTkrWgza9Gy/giphy.gif'>

**You are here!** This site is built with [Hugo](https://gohugo.io), a static site generator written in `go`. No themes are loaded which means all the structure and design is my own. I constructed the view/layout templates with `go`'s amazing [templating language](https://golang.org/pkg/text/template/). The CSS features modern design without relying on a framework by leveraging `flexbox`, gradients, animations, and more. All of the content is kept in `markdown` which makes editing easy.

Here's an example of some `markdown` this page is built with:
{{< highlight md >}}
---
title: Portfolio Website
date: 2019-02-27T18:32:20.407Z
repo: portfolio
demo: 'https://stelabrego.com'
tags:
  - go
  - javascript
  - markdown
  - css
  - html
---
This site is built with [Hugo](https://gohugo.io), a static site generator written in `go`. No themes are loaded which means all the structure and design is my own. I constructed the view/layout templates with `go`'s amazing [templating language](https://golang.org/pkg/text/template/).
{{< /highlight >}}

This is a section of the view template that created this page:
{{< highlight html >}}
{{ partial "head.html" . }}
<h2>{{ .Title }}</h2>
{{ partial "code-tags.html" . }}

{{ with .Params.repo }}
<p>🛠 <a href='https://github.com/stelabrego/{{ . }}' target="_blank">Check out the source code</a> 👷🏻‍♀️</p>
{{ end }}

{{ with .Params.demo }}
<p>✨ <a href='{{ . }}' target="_blank">Check out the live demo</a> ✨</p>
{{ end }}

{{ .Content }}
{{< /highlight >}}
