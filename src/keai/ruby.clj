(ns keai.ruby
  (:require [me.raynes.fs :as fs])
  (:import [org.jruby CompatVersion]
           [org.jruby.embed ScriptingContainer LocalContextScope]))

(defn set-container-version! [c]
  (. c setCompatVersion
     (. CompatVersion RUBY1_9)))

(defonce container
  (let [container (ScriptingContainer. LocalContextScope/CONCURRENT)]
    (set-container-version! container)
    container))

(defn add-gems-to-load-path
  "Takes a path relative to the project root and adds all gems below
  the specified path to the load path.

  Usage:
  cmd-line> bundle install --binstubs=.bin --path=vendor/bundle
  user> (add-gems-to-load-path \"/vendor/bundle/ruby/1.9.1/gems\")
  ;=> nil"
  [gems-dir]
  (let [proj-root (System/getProperty "user.dir")
        qualified-gem-path (str proj-root gems-dir)
        gems (fs/list-dir qualified-gem-path)
        initial-load-paths (.getLoadPaths container)
        load-paths (concat initial-load-paths
                           (for [gem gems]
                             (str qualified-gem-path "/" gem "/lib")))]
    (. container setLoadPaths load-paths)))

(defn _eval [rb-str] (. container runScriptlet rb-str))

(defn _require [& gems]
  (doseq [gem gems]
    (_eval (str "require '" gem "';"))))

(defn _set [variable val]
  (. container put variable val))

(defn _get [variable]
  (. container get variable))

(defn _call
  ([obj method ret] (. container callMethod obj method ret))
  ([obj method arg ret] (. container callMethod obj method arg ret)))


;; Convert org.jruby.RubyObjects to Clojure
(defmulti to-clj class)

(defmethod to-clj :default [obj] obj)

(defmethod to-clj org.jruby.RubyString [obj] (str obj))

(defmethod to-clj org.jruby.RubyArray [obj]
  (mapv to-clj (vec (.toJavaArray obj))))

(defmethod to-clj org.jruby.RubyFixnum [obj]
  (.getLongValue obj))

(defmethod to-clj org.jruby.RubySymbol [obj]
  (keyword (str obj)))

(defmethod to-clj org.jruby.RubyHash [obj]
  (zipmap (mapv to-clj (.keys obj))
          (mapv to-clj (.values obj))))

(defprotocol Coercable
  "Coerce Ruby objects to Clojure types"
  (clj [this] "Coerce a Ruby object to its Clojure representation"))

(extend-protocol Coercable
  org.jruby.RubyString
  (clj [this] (str this))

  java.lang.Long
  (clj [this] this)

  java.lang.String
  (clj [this] this)

  org.jruby.RubyFixnum
  (clj [this] (.getLongValue this))

  org.jruby.RubyArray
  (clj [this] (mapv clj (vec (.toJavaArray this))))

  org.jruby.RubySymbol
  (clj [this] (keyword (str this)))

  org.jruby.RubyHash
  (clj [this] (zipmap (mapv clj (.keys this))
                      (mapv clj (.values this)))))

(comment
  (clj (_eval "{a: 1}"))
  ;; => {:a 1}
  (clj (_eval "{:a => 1, :b => 'c', :x => [1,2,3]}"))
  ;; => {:x [1 2 3], :b "c", :a 1}
)
