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
  (str "(declare " s ")"))

(defn to-str
  [declarations clj-exprs]
  (let [header-str   (pr-str (header 'shen))
        declare-strs (map symbol->declare-str declarations)
        forms-strs   (for [expr clj-exprs]
                       (if (string? expr)
                         (str "(comment \"" expr "\")")
                         (pr-str expr)))]
    (s/join "\n" (cons header-str (concat declare-strs forms-strs)))))

(defn process-file
  [file]
  (let [kl-forms     (kl-reader/read-file (slurp (str kl-path file)))
        declarations (declarations kl-forms)]
    (spit (str clj-path (clj-filename file))
          (to-str declarations (map #(backend/kl->clj [] %) kl-forms)))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (if (first args)
    (println (kl-reader/read-file (slurp (str kl-path (first args)))))
    (doseq [file kl-files]
      (println "Reading" file)
      (process-file file))))
