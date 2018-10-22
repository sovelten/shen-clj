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

(def shen-dot-read-file-as-Xlist-help
  '(shen-port.primitives/with-ns 'shen.functions
     (clojure.core/defn shen-dot-read-file-as-Xlist-help
       ([V2371 V2372 V2373 V2374]
        (clojure.core/cond
          (= -1 V2373) V2374
          true (recur V2371 V2372 ((shen.primitives/resolve-fn V2372) V2371) (cons V2373 V2374))
          :else (throw (Exception. "No matching cond clause"))))
       ([V2371 V2372 V2373]
        (clojure.core/partial shen-dot-read-file-as-Xlist-help V2371 V2372 V2373))
       ([V2371 V2372]
        (clojure.core/partial shen-dot-read-file-as-Xlist-help V2371 V2372))
       ([V2371]
        (clojure.core/partial shen-dot-read-file-as-Xlist-help V2371)))))
