(ns shen-port.core
  (:gen-class)
  (:use [clojure.java.io :only (file reader writer)]
        [clojure.pprint :only (pprint)])
  (:require [clojure.string :as s]
            [shen-port.backend :as backend])
  (:import [java.io StringReader PushbackReader FileNotFoundException]
           [java.util.regex Pattern]))

(def cleanup-symbols-pattern
  (re-pattern (str "(\\s+|\\()("
                   (s/join "|" (map #(Pattern/quote %) [":" ";" "{" "}" ":-" ":="
                                                        "/." "@p" "@s" "@v"
                                                        "shen-@s-macro"
                                                        "shen-@v-help"
                                                        "shen-i/o-macro"
                                                        "shen-put/get-macro"
                                                        "XV/Y"]))
                   ")(\\s*\\)|\\s+?)"
                   "(?!~)")))

(defn cleanup-symbols
  [kl] (s/replace kl
                  cleanup-symbols-pattern
                  "$1(intern \"$2\")$3"))

(defn read-kl [kl]
  (with-open [r (PushbackReader. (StringReader. (cleanup-symbols kl)))]
    (doall
     (take-while (complement nil?)
                 (repeatedly #(read r false nil))))))

(defn read-kl-file [file]
  (try
    (cons `(c/comment ~(str file)) (read-kl (slurp file)))
    (catch Exception e
      (println file e))))

(defn my-read
  [file]
  (slurp file))

(defn -main
  "I don't do a whole lot ... yet."
  [file & args]
  (let [forms (read-string (my-read file))]
    (println forms)
    #_(println (backend/kl->clj [] (first forms)))))
