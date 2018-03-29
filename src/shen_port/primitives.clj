(ns shen-port.primitives
  (:require [clojure.core :as c]
            [shen-port.backend :as backend])
  (:import [java.util Arrays])
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


;;
;; Arithmetic
;;

(defn-curried + [x y] (c/+ x y))
(defn-curried - [x y] (c/- x y))
(defn-curried * [x y] (c/* x y))
(defn-curried / [x y] (c// x y))

;;
;; Conditionals
;;

(defn andp [x]
  (fn [y]
    (c/and x y)))

(defn orp [x]
  (fn [y]
    (c/or x y)))

(defn ifp
  ([test]
   (fn [p1 p2]
     (if test p1 p2)))
  ([test p1]
   (fn [p2]
     (if test p1 p2))))

;;;
;;; Error Handling
;;;

(defn simple-error
  [message]
  (throw (Exception. message)))

(defn error-to-string
  [exception]
  (.getMessage exception))

;;
;; Symbols
;;

(defn intern
  [str]
  (case str
    "true" true
    "false" false
    (symbol str)))

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
;; Strings
;;

(defn-curried pos
  [X N]
  (c/str (get X N)))

(defn tlstr
  [X]
  (subs X 1))

(defn-curried cn
  [Str1 Str2]
  (str Str1 Str2))

(defn string->n
  [S]
  (c/int (first S)))

(defn n->string
  [N]
  (c/str (char N)))

;;
;; Vectors
;;

(def ^:private array-class (Class/forName "[Ljava.lang.Object;"))

(defn absvector [N]
  (doto (object-array (int N)) (Arrays/fill 'fail!)))

(defn absvector? [X]
  (identical? array-class (c/class X)))

(defn <-address [#^"[Ljava.lang.Object;" Vector N]
  (aget Vector (int N)))

(defn address-> [#^"[Ljava.lang.Object;" Vector N Value]
  (aset Vector (int N) Value)
  Vector)

;;
;; Conses
;;

(defn cons?
  [X]
  (c/and (list? X) (= 2 (count X))))

(defn-curried cons
  [X Y]
  (if (cons? Y)
    (list X Y)
    (list X Y)))

(defn hd
  [X]
  (if (cons? X)
    (first X)
    (simple-error "Not a cons")))

(defn tl
  [X]
  (if (cons? X)
      (let [tail (rest X)]
        (if (cons? rem)
          tail
          (first tail)))
      (simple-error "Not a cons")))

;;
;; Function Declarations
;;

;; Not part of the language, but used internally
(set* 'set* #'set* 'shen.primitives)
(set* 'curried-fn #'curried-fn 'shen.primitives)
(set* 'andp #'andp 'shen.primitives)
(set* 'orp #'orp 'shen.primitives)
(set* 'ifp #'ifp 'shen.primitives)

;; KL Functions
(set* 'set #'set 'shen.functions)
(set* 'value #'value 'shen.functions)
(set* '+ #'+ 'shen.functions)
(set* '- #'- 'shen.functions)
(set* '* #'* 'shen.functions)
(set* '/ #'/ 'shen.functions)
(set* 'simple-error #'simple-error 'shen.functions)
(set* 'error-to-string #'error-to-string 'shen.functions)
(set* 'intern #'intern 'shen.functions)
(set* 'symbol? #'c/symbol? 'shen.functions)
(set* 'number? #'c/number? 'shen.functions)
(set* '> #'c/> 'shen.functions)
(set* '>= #'c/>= 'shen.functions)
(set* '< #'c/< 'shen.functions)
(set* '<= #'c/<= 'shen.functions)
(set* 'string? #'c/string? 'shen.functions)
(set* 'pos #'pos 'shen.functions)
(set* 'tlstr #'tlstr 'shen.functions)
(set* 'cn #'cn 'shen.functions)
(set* 'str #'str 'shen.functions)
(set* 'string->n #'string->n 'shen.functions)
(set* 'n->string #'n->string 'shen.functions)
(set* 'absvector #'absvector 'shen.functions)
(set* 'absvector? #'absvector? 'shen.functions)
(set* 'address-> #'address-> 'shen.functions)
(set* '<-address #'<-address 'shen.functions)
(set* 'cons? #'cons? 'shen.functions)
(set* 'cons #'cons 'shen.functions)
(set* 'hd #'hd 'shen.functions)
(set* 'tl #'tl 'shen.functions)

(defn eval-kl
  [X]
  (eval (doto (backend/kl->clj [] X) println)))

