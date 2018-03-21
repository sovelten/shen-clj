(ns shen-port.prim
  (:require [clojure.core :as c]
            [clojure.java.io :as io]
            [clojure.set :as set]
            [clojure.string :as s]
            [clojure.walk :as w])
  (:refer-clojure :exclude [set intern let pr type cond cons str number? string? defmacro
                            + - * / > < >= <= = and or]))

;;
;; Utils (currying)
;;

(defn ^:private curry
  [[params1 params2] body]
  (c/cons (vec params1)
          (if (empty? params2)
            body
            (list (apply list 'fn (vec params2) body)))))

(defn ^:private do-curried [symbol to-fn params]
  (c/let [result (split-with (complement vector?) params)
          [[name doc meta] [args & body]] result
          [doc meta] (if (c/string? doc) [doc meta] [nil doc])
          body (if meta (c/cons meta body) body)
          arity-for-n #(-> % inc (split-at args) (to-fn body))
          arities (->>
                   (range 0 (count args))
                   (map arity-for-n)
                   reverse)
          before (keep identity [symbol name doc])]
    (concat before arities)))

(c/defmacro fn-curried
  "Builds a multiple arity function similar that returns closures
          for the missing parameters, similar to ML's behaviour."
  [& params]
  (do-curried 'fn curry params))

;;
;; Symbols
;;

(create-ns 'shen)
(create-ns 'shen.globals)
(create-ns 'shen.functions)

(def intern symbol)

;;
;; Assignments
;;

(defn set* [X Y ns]
  @(c/intern (the-ns ns)
             (with-meta X {:dynamic true :declared true})
             Y))

(defn set
  ([X] (partial set X))
  ([X Y] (set* X Y 'shen.globals)))

;;declare set on 'shen.functions
(set* 'set #'set 'shen.functions)

(defn ^:private value* [X ns]
  (c/let [v (c/and (symbol? X) (ns-resolve ns X))]
    (if (nil? v)
      (throw (IllegalArgumentException. (c/str "variable " X " has no value")))
      @v)))

(defn value [X] (value* X 'shen.globals))

;;declare value on 'shen.functions
(set* 'value #'value 'shen.functions)

;;
;; Functions
;;

(c/defmacro lambda
  [x y]
  (list 'fn [x] y))

(c/defmacro agoravai
  [x y] `(list 'fn [~x] ~y))

(c/defmacro agoravai2
  [args & body]
  `(fn [~args] ~@body))

(c/defmacro lambda3 [X Y]
  `(fn [~X & XS#]
     (c/let [result# ~Y]
       (if XS# (apply result# XS#)
           result#))))

(c/defmacro lambda4 [X Y]
  `(lambda3 ~X ~Y))

(c/defmacro lambda2
  [x y]
  `(do
     (println ~x)
     (println ~y)
     (fn [~x & XS#]
       (c/let [result# ~y]
         (if XS# (apply result# XS#)
             result#))))
  #_(list 'fn (eval x) (eval y)))

(set* '+ #'c/+ 'shen.functions)

(c/defmacro defun [name arglist body]
  (list 'set*
        (list 'quote name)
        (list 'fn-curried (vec arglist) body)
        (list 'quote 'shen.functions)))

;;
;; Evaluation
;;

(defn add-ns [x]
  (if (ns-resolve 'shen-port.prim x)
    (ns-resolve 'shen.functions x)
    (ns-resolve 'shen.functions x)))

(defn ^:private resolve-list
  [L]
  ())

(defn quote-symbols
  [X]
  (println X)
  (c/cond
    (symbol? X) (list 'quote X)
    (c/= () X)  X
    (list? X) (c/let [[fst & rest] X]
                (c/cond
                  (empty? rest) (list (add-ns fst))
                  ('#{lambda} fst) 1
                  ('#{defun} fst) (eval X)
                  :else (c/cons (add-ns fst)
                                (for [y rest]
                                  (quote-symbols y)))))
    :else X))

;;
;; Arithmetic
;;

(defn eval-kl-2
  [X]
  (c/cond
    (symbol? X) X #_(eval (list 'quote X))
    (c/number? X) X
    ((some-fn true? false?) X) X
    (c/= () X)  X
    (list? X) (c/let [[fst snd trd & rest] X]
                (c/cond
                  ('#{lambda} fst) (agoravai2 snd trd)
                  :else (eval (c/cons (eval-kl-2 fst)
                                      (for [y rest] (eval-kl-2 y))))))
    :otherwise (throw (Exception. (c/str "Can not parse '" X)))))

#_(defn eval-kl
  [X]
  (c/cond
    (symbol? X) (eval (list 'quote X))
    (c/number? X) X
    ((some-fn true? false?) X) X
    (c/= () X)  X
    (list? X) (c/let [[fst & rest] X]
                (c/cond
                  ('#{defun} fst) (eval X)
                  :else (eval (c/cons (add-ns (eval-kl fst))
                                      (for [y rest] (eval-kl y))))))
    :else (throw (Exception. (c/str "Can not parse '" X)))))

(defn eval-kl [expr]
    (eval (quote-symbols expr)))
