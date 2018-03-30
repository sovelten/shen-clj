(ns shen-port.kl-reader
  (:require [instaparse.core :as insta]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [read]))

(def transform-options
  {:number read-string
   :symbol symbol
   :string identity
   :list   list})

(def parser
  (insta/parser (io/file "resources/kl.grammar")))

(defn read
  [input]
  (let [parsed-tree (parser input)]
    (second (insta/transform transform-options parsed-tree))))
