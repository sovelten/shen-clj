(ns shen-port.core
  (:gen-class)
  (:use [clojure.java.io :only (file reader writer)]
        [clojure.pprint :only (pprint)])
  (:require [clojure.string :as s]
            [shen-port.backend :as backend]
            [shen-port.kl-reader :as kl-reader])
  (:import [java.io StringReader PushbackReader FileNotFoundException]
           [java.util.regex Pattern]))

(defn my-read
  [file]
  (slurp file))

(defn -main
  "I don't do a whole lot ... yet."
  [file & args]
  (let [forms (kl-reader/read-file (str "(" (slurp file) ")"))]
    (println forms)
    #_(println (backend/kl->clj [] (first forms)))))
