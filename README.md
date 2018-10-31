# Dired for Liquid
Liquid extension for navigating and manipulating files in Liquid.  
The extension is usable, but feature are few so far.

## To install
The [liquid-starter-kit](https://github.com/mogenslund/liquid-starter-kit) can be used as example and in general as a way to setup Liquid locally.

In the deps.edn include this project in the deps section, like

    {:deps mogenslund/dired {:git/url "https://github.com/mogenslund/dired.git"
                             :tag "v0.1.0"}}

(Maybe with an updated tag, to get the newest version)

In your project require `[dk.salza.dired :as dired]` and assign a keyboad shortcut like this:

    (editor/set-global-key "f6" #(dired/run (editor/get-folder)))

or add dired to `C-space` typeahead using this:

    (editor/add-interactive "dired" #(dired/run (editor/get-folder))) 