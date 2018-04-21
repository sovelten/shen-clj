(ns shen-port.overwrite
  (:require [shen-port.primitives :refer :all])
  (:refer-clojure :only []))

;;
;; Replace for function with tail call optimization (until we support tail call optimization)
;;

(def shen-dot-fillvector
  '(shen-port.primitives/with-ns 'shen.functions
     (clojure.core/defn shen-dot-fillvector
       ([Vec pos c d]
        (clojure.core/cond (= c pos) (address-> Vec c d)
                           :else   (recur (address-> Vec pos d) (+ 1 pos) c d)))
       ([Vec pos c]
        (clojure.core/partial shen-dot-fillvector Vec pos c))
       ([Vec pos]
        (clojure.core/partial shen-dot-fillvector Vec pos))
       ([Vec]
        (clojure.core/partial shen-dot-fillvector Vec)))))
