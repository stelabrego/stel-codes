CSS layouts are hard. That's not a very controversial opinion! It's difficult to position items exactly as intended, and it's even harder to write CSS code that's readable afterwards. For instance, the second most upvoted CSS question on Stack Overflow is ["How to horizontally center an element"](https://stackoverflow.com/questions/114543/how-to-horizontally-center-an-element).

The CSS [Flexbox Box Layout Module](https://developer.mozilla.org/en-US/docs/Learn/CSS/CSS_layout/Flexbox) became widely supported [around 2014](https://caniuse.com/flexbox). This was a major improvement over past layout techniques. However, flexbox was mostly a way to build layouts in one direction: either horizontally or vertically. For this purpose, flexbox is great. But screens have two dimensions! So of course there was still a need for a CSS layout module that could position items both horizontally and vertically while handling all the complexities that come with that power.

That's where the [CSS Grid Layout Module](https://developer.mozilla.org/en-US/docs/Web/CSS/grid) comes into the mix. It became widely supported [around 2017](https://caniuse.com/css-grid). The CSS Grid properties finally allow developers to write two-dimesional layouts in a concise, readable fashion.

However, CSS Grid is complex. It definitely takes some time to get used to. Even after writing several website layouts using the grid layout module, I still have to refer back to reference websites. Here's a [great MDN introduction](https://developer.mozilla.org/en-US/docs/Learn/CSS/CSS_layout/Grids) and my [favorite CSS Grid cheatsheet](https://grid.malven.co/).

I find that one of the best ways for me to learn a new technology is to read a book about it. I came across "Grid Layout in CSS" and I had to pick it up! I'm really glad I did. This book is a thorough guide into the complexities of CSS Grid. It features many diagrams and examples which illustrate the power and nuances hidden within.

One of my favorite things about this book is that Eric Meyer goes into the "why" behind the CSS Grid standard. I learn much better when someone can tell me the "why" behind what I'm learning.

I also love that occasionally Eric will critique the standard! For example, he opines that although `fr` values are not currently allowed as minimums, they should be and probably will in the future. Later, when Eric talks about the `grid` property, Eric writes my favorite passage in the whole book:

> The syntax is a little bit migraine-inducing, I admit, but we’ll step through it a piece at a time.
>
> Let’s get to the elephant in the room right away: grid allows you to either define a grid template or you can set the grid’s flow and auto-track sizing in a compact syntax. You can’t do both at the same time.
>
> Furthermore, whichever you don’t define is reset to its defaults, as is normal for a shorthand property. So if you define the grid template, then the flow and auto tracks will be returned to their default values. This includes grid gutters, a topic we haven’t even covered yet. You can’t set the gutters with grid, but it will reset them anyway.
>
> Yes, this is intentional. No, I don’t know why.

Most coding guides will just tell you how it is. But I love the commentary! It helps me contextualize the design decisions. Humans aren't perfect and *occassionally* we make poor abstractions. Ok, maybe not so occassionally. It's good to know when something doesn't quite make sense to the experts on the topic either!

Grid is my go-to layout tool nowadays. Reading this book helped me realize that CSS Grid can basically do everything Flexbox can do, but much much more. So lately I've been avoiding Flexbox altogether and using Grid for all my layout needs. The result is much more readable S/CSS files and less "gotchas" that frequently plague writers of CSS, both beginner and experts alike.

If you're interested in using CSS Grid, definitely give this book a read! I think it's a great reference and I'll definitely be revisiting in the future.
