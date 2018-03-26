(ns shen-port.primitives
  (:require [clojure.core :as c]
            [shen-port.backend :as backend])
  (:refer-clojure :exclude [set intern]))

(create-ns 'shen.globals)
(create-ns 'shen.functions)
(create-ns 'shen.primitives)

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

(defn curried-fn
  [params]
  (eval (do-curried 'fn curry params)))

(defmacro defn-curried
  [& params]
  (do-curried 'defn curry params))

(def intern symbol)

(defn set* [X Y ns]
  @(c/intern (the-ns ns)
             (with-meta X {:dynamic true :declared true})
             Y))

(defn set
  ([X] (partial set X))
  ([X Y] (set* X Y 'shen.globals)))

(defn ^:private value* [X ns]
  (c/let [v (c/and (symbol? X) (ns-resolve ns X))]
    (if (nil? v)
      (throw (IllegalArgumentException. (c/str "variable " X " has no value")))
      @v)))

(defn value [X] (value* X 'shen.globals))

;;
;; Arithmetic
;;

(defn-curried + [x y] (c/+ x y))
(defn-curried - [x y] (c/- x y))
(defn-curried * [x y] (c/* x y))
(defn-curried / [x y] (c// x y))

;;
;; Function Declarations
;;

;; Not part of the language, but used internally
(set* 'set* #'set* 'shen.primitives) 
(set* 'curried-fn #'curried-fn 'shen.primitives)

;; KL Functions
(set* 'set #'set 'shen.functions)
(set* 'value #'value 'shen.functions)
(set* '+ #'+ 'shen.functions)
(set* '- #'- 'shen.functions)
(set* '* #'* 'shen.functions)
(set* '/ #'/ 'shen.functions)

(defn eval-kl
  [X]
  (eval (doto (backend/kl->clj [] X) println)))

