(ns shen-port.install
  (:gen-class)
  (:require [clojure.string :as s]
            [shen-port.backend :as backend]
            [shen-port.kl-reader :as kl-reader]
            [shen-port.overwrite :as overwrite]
            [clojure.string :as str])
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
(def clj-path "src/shen/")

(def overwrites #{'shen-dot-fillvector
                  'shen-dot-aum_to_shen
                  'shen-dot-aum_to_shen-aux
                  #_'shen-dot-th*
                  #_'shen-dot-t*-hyps})

(defn overwrite->str
  [x]
  (pr-str @(ns-resolve (the-ns 'shen-port.overwrite) (symbol x))))

(defn overwrites->str
  []
  (s/join "\n" (map #(overwrite->str %) overwrites)))

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
                         (str "(clojure.core/comment \"" expr "\")")
                         (pr-str expr)))]
    (s/join "\n" forms-strs)))

(defn process-file
  [content filename]
  (println "Reading" filename)
  (let [kl-forms     (->> (kl-reader/read-file content)
                          (remove #(contains? overwrites (function-name %))))
        kl-forms'    (remove #(contains? overwrites (function-name %)))
        declarations (declarations kl-forms)
        output-str   (str "(clojure.core/comment " filename ")\n"
                          (to-str (map #(backend/kl->clj [] %) kl-forms)))]
    [declarations output-str]))

(defn declarations->str
  [declarations]
  (str (s/join "\n" (map symbol->declare-str declarations)) "\n"))

(defn -main
  [& args]
  (let [contents     (mapv #(slurp (str kl-path %)) kl-files)
        results      (mapv #(process-file %1 %2) contents kl-files)
        header       (str "(ns shen.functions\n"
                          "(:require [shen-port.primitives :as p])\n"
                          "(:refer-clojure :only []))\n")
        declarations (declarations->str (mapcat first results))
        overwrites   (overwrites->str)
        code         (s/join "\n" (map second results))
        main         "(clojure.core/defn -main [] (shen-dot-shen))"
        output       (s/join "\n" [header declarations overwrites code main])]
    (spit (str clj-path "functions.clj") output)))
