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

(defn cond-clause
  [locals clause]
  (let [[test body] clause]
    (match clause
           ([_ _] :seq) 0
           :else (throw (Exception. "Clause must have only two forms")))))

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

    ; Locals [let X Y Z] -> (let ChX (ch-T X) (protect [LET [[ChX (kl-to-lisp Locals Y)]] (kl-to-lisp [ChX | Locals] (SUBST ChX X Z))]))
    (match? expr (['let _ _ _] :seq)) (let [[_ x y z] expr]
                                       (list 'let [x (kl->clj locals y)] (kl->clj (conj locals x) z)))

    ; _ [defun F Locals Code] -> (protect [DEFUN F Locals (kl-to-lisp Locals Code)])
    (match? expr (['defun _ _ _] :seq)) (let [[_ name vars body] expr]
                                          (list 'defn name (into [] vars) (kl->clj vars body)))

    ; Locals [cond | Cond] -> (protect [COND | (MAPCAR (/. C (cond_code Locals C)) Cond)])
    (match? expr (['cond _] :seq)) (let [[_ fst & clauses] expr]
                                    (list 'cond))



    :else expr))
