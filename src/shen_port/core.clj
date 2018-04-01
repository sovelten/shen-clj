(ns shen-port.core
  (:gen-class)
  (:use [clojure.java.io :only (file reader writer)]
        [clojure.pprint :only (pprint)])
  (:require [clojure.string :as s]
            [shen-port.backend :as backend]
            [shen-port.kl-reader :as kl-reader])
  (:import [java.io StringReader PushbackReader FileNotFoundException]
           [java.util.regex Pattern]))


(def kl-files
  ["toplevel.kl"
   "core.kl"
   "sys.kl"
   "dict.kl"
   "sequent.kl"
   "yacc.kl"
   "reader.kl"
   "prolog.kl"
   "track.kl"
   "load.kl"
   "writer.kl"
   "macros.kl"
   "declarations.kl"
   "t-star.kl"
   "types.kl"])

(def kl-path "resources/klambda/")
(def clj-path "resources/shen-clj/")

(defn clj-filename
  [file]
  (let [splitted (s/split file #"\.")]
    (str (first splitted) ".clj")))

(defn function-name
  [[fst snd & rest]]
  (when (= fst 'defun)
    snd))

(defn declarations
  [expr]
  (remove nil? (map function-name expr)))

(defn header [ns]
  `(~'ns ~ns
    (:require [clojure.core :as ~'c]
              [shen.primitives :refer :all])
    (:refer-clojure :exclude [set intern cons + - / *])
    (:gen-class)))

(defn symbol->declare-str
  [s]
  (str "(clojure.core/declare " s ")"))

(defn to-str
  [clj-exprs]
  (let [header-str   (pr-str (header 'shen))
        forms-strs   (for [expr clj-exprs]
                       (if (string? expr)
                         (str "(comment \"" expr "\")")
                         (pr-str expr)))]
    (s/join "\n" forms-strs)))

(defn process-file
  [content filename]
  (println "Reading" filename)
  (let [kl-forms     (kl-reader/read-file content)
        declarations (declarations kl-forms)
        output-str   (to-str (map #(backend/kl->clj [] %) kl-forms))]
    #_(spit (str clj-path (clj-filename file)) output-str)
    #_(spit (str clj-path "clj-declarations.clj") output-str)
    [declarations output-str]))

(defn write-declarations
  [declarations]
  (spit (str clj-path "clj-declarations.clj")
        (str "(ns shen.functions)\n" (s/join "\n" (map symbol->declare-str declarations)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [contents     (mapv #(slurp (str kl-path %)) kl-files)
        results      (mapv #(process-file %1 %2) contents kl-files)
        declarations (mapcat first results)
        output       (s/join "\n" (map second results))]
    (write-declarations declarations)
    (spit (str clj-path "clj-declarations.clj") output :append true)))
