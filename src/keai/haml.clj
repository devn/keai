(ns keai.haml
  (:require [clojure.java.io :as io]
            [keai.ruby :as rb]))

(defonce load-haml
  (do (rb/add-gems-to-load-path "/vendor/bundle/ruby/1.9.1/gems")
      (rb/_require "rubygems" "haml")))

(defmulti to-haml class)

(defmethod to-haml java.lang.String [template]
  (-> (rb/_call (rb/_eval "Haml::Engine") "new" template Object)
      (rb/_call "render" template String)))

(defmethod to-haml java.io.File [template]
  (let [tmpl (slurp template)]
    (-> (rb/_call (rb/_eval "Haml::Engine") "new" tmpl Object)
        (rb/_call "render" tmpl String))))

(comment
  (to-haml (java.io.File. "/Users/dmw/dev/keai/test.haml"))
  (to-haml "!!!")
)
