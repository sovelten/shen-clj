(ns shen-port.kl-reader
  (:require [instaparse.core :as insta]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [read]))

(def parser-grammar (slurp "resources/kl.grammar"))

(def parser
  (insta/parser parser-grammar))

(def file-parser
  (insta/parser (str "file = klexpr+ <whitespace>*"
                     parser-grammar)))

(def transform-options
  {:number read-string
   :symbol symbol
   :string identity
   :list   list})

(defn clj-sexp
  [parsed-tree]
  (second (insta/transform transform-options parsed-tree)))

(defn read
  [input-str]
  (clj-sexp (parser input-str)))

(defn read-file
  [input-str]
  (let [[_ & exprs] (file-parser input-str)]
    (map clj-sexp exprs)))
