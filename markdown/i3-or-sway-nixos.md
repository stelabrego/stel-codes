I've been using [Sway](https://swaywm.org) on [NixOS](https://nixos.org) for about 6 months, and I love it. But some sofware doesn't yet play nice with it. After having major issues with [OBS Studio](https://obsproject.com/)'s Wayland support, I decided to switch to [i3](https://i3wm.org), Sway's spiritual ancestor. But I wanted to know, could I have the option of choosing either one? I discovered the answer is yes! You definitely can.

![An old school computer window diagram](https://s3.stel.codes/ad669870-9bf9-46e9-bc8d-96d670a8cd86.png)

## A Window into What I'm Talking About

*If you already know lots about Sway and i3, scroll to the next section!*

In this post I'm talking about [windowing systems](https://en.wikipedia.org/wiki/Windowing_system), programs that control the way visual windows are navigated and displayed on screen. MacOS and Windows have their own proprietary windowing systems that cannot be replaced, but Linux lets you freely choose which to use. Created in 1984, the [X Window System](https://en.wikipedia.org/wiki/X_Window_System) (often called X11, or simply X) is the classic choice for Linux. But now, nearly 40 years later, X11 shows it age.

A new windowing system has emerged called [Wayland](https://en.wikipedia.org/wiki/Wayland_\(display_server_protocol\)), and it simplifies the protocol and architecture of X11. They share many similarities. Both X11 and Wayland use a client-server model. The clients (terminals, web browsers, media players, etc) communicate with a central server via [Unix Domain Sockets](https://en.wikipedia.org/wiki/Unix_domain_socket). However, Wayland communicates more directly with GPU's, combines the server and compositor into one, and provides a simpler API for client apps to implement. The result is smoother scrolling and less resource usage. Ubuntu and Fedora now ship with Wayland [as the default](https://linuxiac.com/fedora-34-released-with-gnome-40-and-wayland-by-default). Many Linux users are starting to accept that Wayland is the future. Adam Jackson, one of the lead maintainers of X11 in 2020, wrote an insightful [blog post](https://ajaxnwnk.blogspot.com/2020/10/on-abandoning-x-server.html) about some of X11's core issues, even calling it "deeply flawed" and "abandonware".

Now let's talk about window *managers*. Window managers commonly refers to programs that speak with the windowing system. They are many different flavors. i3 is a minimalistic *tiling* window manager that uses the X11 windowing system. Sway is a reimplementation of i3 that uses the Wayland windowing system. To an end user, Sway and i3 are almost exactly the same. In fact, many lines of configuration (keybindings, workspaces) can be effortlessly shared between the two! Personally, I love the simplicity and ease of configuration these window managers provide. Every command can be executed via keybindings, making a mouse completely unecessary for window control.

## OBS Not Yet Vibing with Wayland

Since Sway is based on the newer Wayland system, many Linux applications have a hard time running on it. One of these is OBS Studio, or OBS for short. As a program that records screen captures, OBS often speaks with the windowing system. It just doesn't work well with Wayland yet. As of version 27, Wayland is officially supported, but full support is still far away. I had trouble getting any screen or webcam capturing to work. Parts of the UI are not visibile without launching OBS with special environment variables. Unfortunately, OBS was not even close to usable for me on Sway. I really want to start making YouTube coding tutorials, so I decided to switch to the i3 window manager, hoping OBS would work significantly better on X11 (spoiler alert: it does).

## Why Not Both??

After getting an i3 setup working with OBS, I started to wonder: Can I have both i3 *and* Sway available? Can I choose one or the other on startup? I discovered that on NixOS, this is actually really simple. But I didn't figure that out at first.

My first approach was to make a command that executes i3 from a tty. I was starting Sway with a shell alias, `gui`, that executed `exec sway` command from the tty after authenticating with logind. My roommates said I looked like a hacker whenever I was logging in from boot. I loved that! But after some trial and error, I discovered that using a login manager (also called display manager) was the easier option for a dual i3+sway setup on NixOS. The reason being that login managers often come with the option for different graphical sessions using different window managers. The login manager I'm using now is called LightDM. By clicking a button in the top right corner, I can select whether I want to launch an i3 or Sway session. Perfect!

So how does one create such a setup? Most Linux distros require a series of shell commands to install and configure core software. NixOS is a little different. It uses a code as configuration instead. These are the configuration lines in my `configuration.nix` that allow me to run this sweet i3+Sway setup:

```
# i3 config
services.xserver.enable = true;
services.xserver.libinput.enable = true;
services.xserver.desktopManager.xterm.enable = false;
services.xserver.displayManager.lightdm.enable = true;
services.xserver.displayManager.defaultSession = "none+i3";
services.xserver.windowManager.i3.enable = true;
services.xserver.windowManager.i3.configFile = ./i3-config;

# Sway config
programs.sway.enable = true;
environment.etc."sway-config".source = ./sway-config;
```

Just change `./i3-config` and `./sway-config` to the location of your configuration files and create a symbolic link to your Sway config by running this command:
```
mkdir -p $HOME/.config/sway && ln -s /etc/sway-config $HOME/.config/sway/config
```

And that's it! Putting these lines of code into your NixOS configuration will give you the exact same setup. 🥳

The reason why this is **so** simple on NixOS is because both the `services.xserver.windowManager.i3` [module](https://github.com/NixOS/nixpkgs/blob/nixos-21.05/nixos/modules/services/x11/window-managers/i3.nix) and `programs.sway` [module](https://github.com/NixOS/nixpkgs/blob/nixos-21.05/nixos/modules/programs/sway.nix) add graphical session entries to the `services.xserver.displayManager` [module](https://github.com/NixOS/nixpkgs/blob/nixos-21.05/nixos/modules/services/x11/display-managers/default.nix). This module [creates](https://github.com/NixOS/nixpkgs/blob/2262d7863a6af007274a698367484bf4903a3299/nixos/modules/services/x11/display-managers/default.nix#L419) the necessary `.desktop` files that login/display managers like LightDM use to generate their graphical session list.

Because of this convention, any window manager that can be installed via a NixOS option will likely be added to your login/display manager's option list automatically. From a quick NixOS [option search](https://search.nixos.org/options?channel=21.05&from=0&size=50&sort=relevance&query=services.xserver.windowmanager), it looks like there at least 50 other window managers I could add to my session list. Enabling each one would take a few lines of Nix code at most. I feel like this is an excellent example of how powerful the NixOS abstraction can be. Of course, none of this would be possible without all the hard work donated by all my fellow Nixpkg [maintainers](https://github.com/NixOS/nixpkgs/blob/nixos-unstable/maintainers/maintainer-list.nix).

Happy window managing! 🪟
