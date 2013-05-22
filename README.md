# keai

Use redbridge and JRuby to access Ruby and Ruby gems from Clojure.

## Setup

* Get `rbenv` or `rvm`.
* Get `leiningen` if you don't already have it.
* Clone this repo.
* `cd` into `keai`
* `gem install bundler`
* `bundle install --binstubs=.bin --path=vendor/bundle`
* `lein deps`

## Usage

```clojure
(ns myproject.core
  (:require [keai.ruby :as rb]))

(rb/to-clj (rb/_eval "[1,2,3]"))
;=> [1 2 3]

;; NOTE: The above is actually a vector containing the java.lang.Long
;; values 1, 2, and 3. This is not implemented for all Ruby types.
;; Consider this a WIP.

(rb/to-clj (rb/_eval "{:a => 1, :b => [1,'z',3]}"))
;=> {:a 1, :b [1 'z' 3]}
```

```clojure
(ns myproject.haml
  (:use [keai.haml]))

(to-haml "!!!") ;=> "<!DOCTYPE html>"
(to-haml (java.io.File. "/path/to/a/haml/template.haml")) ;=> "<!DOCTYPE html>"
```

## License

Copyright © 2013 Devin Walters

Distributed under the Eclipse Public License, the same as Clojure.
