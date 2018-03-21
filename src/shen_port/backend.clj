(ns shen-port.backend
  (:require [clojure.core.match :refer [match]]))

(defmacro match?
  [vars clause]
  `(match ~vars
          ~clause true
          :else false))

(defn subst
  [x y expr]
  (if (= expr x)
    y
    (replace {x y} expr)))

(defn kl->clj
  [locals expr]
  (cond

    (some #{expr} locals) (do (println "HEY")
                              expr)

    ; Locals [type X _] -> (kl-to-lisp Locals X)
    (match? expr (['type _ _] :seq)) (kl->clj locals (second expr))

    ; Locals [lambda X Y] -> (let ChX (ch-T X) (protect [FUNCTION [LAMBDA [ChX] (kl-to-lisp [ChX | Locals] (SUBST ChX X Y))]]))
    (match? expr (['lambda _ _] :seq)) (let [[_ x y] expr]
                                         (list 'fn [x] (kl->clj (conj locals x) y)))

    :else expr))
