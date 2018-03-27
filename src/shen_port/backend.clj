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

(defn function-symbol
  [x]
  (symbol (str "shen.functions/" x)))

(defn internal-fn-symbol
  [x]
  (symbol (str "shen.primitives/" x)))

(defn kl->clj
  [locals expr]
  (cond

    (some #{expr} locals) expr

    ; Locals [type X _] -> (kl-to-lisp Locals X)
    (match? expr (['type _ _] :seq))
    (kl->clj locals (second expr))

    ; Locals [lambda X Y] -> (let ChX (ch-T X) (protect [FUNCTION [LAMBDA [ChX] (kl-to-lisp [ChX | Locals] (SUBST ChX X Y))]]))
    (match? expr (['lambda _ _] :seq))
    (let [[_ x y] expr]
      (list 'fn [x] (kl->clj (conj locals x) y)))

    ; Locals [let X Y Z] -> (let ChX (ch-T X) (protect [LET [[ChX (kl-to-lisp Locals Y)]] (kl-to-lisp [ChX | Locals] (SUBST ChX X Z))]))
    (match? expr (['let _ _ _] :seq))
    (let [[_ x y z] expr]
      (list 'let [x (kl->clj locals y)] (kl->clj (conj locals x) z)))

    ; _ [defun F Locals Code] -> (protect [DEFUN F Locals (kl-to-lisp Locals Code)])
    (match? expr (['defun _ _ _] :seq))
    (let [[_ name vars body] expr]
      (list (internal-fn-symbol 'set*)
            (list 'quote name)
            (list (internal-fn-symbol 'curried-fn) (list 'quote (list (into [] vars) (kl->clj vars body))))
            (list 'quote 'shen.functions)))

    ; Locals [cond | Cond] -> (protect [COND | (MAPCAR (/. C (cond_code Locals C)) Cond)])
    (and (list? expr) (= (first expr) 'cond))
    (let [[_ & clauses] expr
          clauses' (concat clauses '((:else "TODO")))]
      (cons 'cond (mapcat (fn [[test body]] (list (kl->clj locals test) (kl->clj locals body))) clauses')))

    (match? expr (['freeze _] :seq))
    (let [[_ x] expr]
      (list 'fn [] (kl->clj locals x)))

    (match? expr (['and _] :seq))
    (let [[_ x] expr]
      (list (internal-fn-symbol 'andp) (kl->clj locals x)))

    (match? expr (['and _ _] :seq))
    (let [[_ x y] expr]
      (list 'and (kl->clj locals x) (kl->clj locals y)))

    (match? expr (['or _] :seq))
    (let [[_ x] expr]
      (list (internal-fn-symbol 'orp) (kl->clj locals x)))

    (match? expr (['or _ _] :seq))
    (let [[_ x y] expr]
      (list 'or (kl->clj locals x) (kl->clj locals y)))

    (match? expr (['if _ _ _] :seq))
    (let [[_ x y z] expr]
      (list 'if (kl->clj locals x) (kl->clj locals y) (kl->clj locals z)))

    (match? expr (['if _] :seq))
    (let [[_ x] expr]
      (list (internal-fn-symbol 'ifp) (kl->clj locals x)))

    (match? expr (['if _ _] :seq))
    (let [[_ x y] expr]
      (list (internal-fn-symbol 'ifp) (kl->clj locals x) (kl->clj locals y)))

    ; _ [] -> []
    (= '() expr)
    '()

    (list? expr)
    (let [[fst & rest] expr
          fname (if (list? fst)
                  (kl->clj locals fst) ;TODO: what happens if this evaluates to a symbol?
                  (function-symbol fst))]
      (cons fname (for [arg rest]
                     (kl->clj locals arg))))

    ; _ S -> (protect [QUOTE S])  where (protect (= (SYMBOLP S) T))
    (symbol? expr)
    (list 'quote expr)

    :else expr))

(defn shen-boolean
  [x]
  (cond
    (= x 'true)  'true
    (= x 'false) 'false
    :else (throw (Exception. "Boolean expected"))))

(defn wrap
  [expr]
  (cond
    (match? expr (['cons? _] :seq)) (let [x (second expr)]
                                      (list 'boolean (list 'not-empty x)))

    (match? expr (['string? _] :seq)) expr
    (match? expr (['number? _] :seq)) expr
    (match? expr (['empty? _] :seq))  expr

    (match? expr (['and _ _] :seq)) (let [[_ x y] expr]
                                      (list 'and (wrap x) (wrap y)))

    (match? expr (['or _ _] :seq)) (let [[_ x y] expr]
                                     (list 'or (wrap x) (wrap y)))

    (match? expr (['not _] :seq)) (let [[_ x] expr]
                                    (list 'not (wrap x)))

    (match? expr (['= _ ([] :seq)] :seq)) (let [[_ x _] expr]
                                    (list 'empty? x))

    (match? expr (['= ([] :seq) _] :seq)) (let [[_ _ y] expr]
                                    (list 'empty? y))

    :else (list 'shen-boolean expr)))
