(ns shen-port.kl-reader
  (:require [instaparse.core :as insta]
            [clojure.string :as s]
            [clojure.java.io :as io])
  (:refer-clojure :exclude [read]))

(def parser-grammar (slurp "resources/kl.grammar"))

(def parser
  (insta/parser parser-grammar))

(def file-parser
  (insta/parser (str "file = klexpr+ <whitespace>*"
                     parser-grammar)))

(def replacements
  [[#"@"  "-at-"]
   [#"\." "-dot-"]
   [#":"  "-colon-"]
   [#"\{"  "-lcurlybrac-"]
   [#"\}"  "-rcurlybrac-"]
   [#";"  "-semicol-"]
   [#"\["  "-lsqrbrac-"]
   [#"\]"  "-rsqrbrac-"]
   [#","  "-comma-"]])

(defn replace-all
  [x replacements]
  (if (empty? replacements)
    x
    (let [[match replacement] (first replacements)]
      (recur (s/replace x match replacement) (rest replacements)))))

(defn replace-slash
  [x]
  (if (= x "/") x (s/replace x #"/" "-slash-")))

(defn kl-symbol
  [x]
  (case x
    "true"  true
    "false" false
    (symbol (replace-all (replace-slash x) replacements))))

(def transform-options
  {:number read-string
   :symbol kl-symbol
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
