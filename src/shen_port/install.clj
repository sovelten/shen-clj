(ns shen-port.install
  (:require [shen-port.primitives :refer :all]
            [clojure.core :as c])
  (:refer-clojure :only []))

(def shen-clj-filepath "resources/shen-clj/")


(def clj-files
  ["toplevel.clj"
   "core.clj"
   "sys.clj"
   "dict.clj"
   "sequent.clj"
   "yacc.clj"
   "reader.clj"
   "prolog.clj"
   "track.clj"
   "load.clj"
   "writer.clj"
   "macros.clj"
   "declarations.clj"
   "t-star.clj"
   "types.clj"])

(c/load-file (c/str shen-clj-filepath "shen.clj"))
