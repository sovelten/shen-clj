(ns shen-port.backend
  (:require [clojure.core.match :refer [match]]
            [clojure.tools.analyzer.jvm :as ana.jvm]
            [clojure.tools.analyzer.passes.jvm.emit-form :as e]))

(fn [x] (do (def pomba)
            (pomba x)))

(defmacro match?
  [vars clause]
  `(match ~vars
          ~clause true
          :else false))

(defn function-symbol
  [x]
  (symbol (str "shen.functions/" x)))

(defn internal-fn-symbol
  [x]
  (symbol (str "shen.primitives/" x)))

(declare kl->clj)
(create-ns 'shen.functions)

(def free-vars (atom #{}))

(defn unresolvable-handler
  [_ x _]
  (swap! free-vars conj x)
  nil)

(defn declare-unresolvable-symbols
  [locals form]
  (reset! free-vars [])
  (ana.jvm/analyze form
                   (assoc (ana.jvm/empty-env)
                          :ns (the-ns 'shen.functions))
                   {:passes-opts (assoc ana.jvm/default-passes-opts
                                        :validate/unresolvable-symbol-handler
                                        unresolvable-handler)})
  (let [free-vars' (distinct (remove (fn [x] (some #{x} locals)) @free-vars))]
    (if (not-empty free-vars')
      (list 'do
            (cons 'clojure.core/declare free-vars')
            form)
      form)))

(defn uncurried-fn
  [name vars body]
  (list 'shen-port.primitives/with-ns
        (list 'quote 'shen.functions)
        (declare-unresolvable-symbols [] (list 'clojure.core/defn
                                               name
                                               (into [] vars)
                                               (kl->clj vars body)))))

(defn curried-fn
  [name vars body]
  (list 'shen-port.primitives/with-ns
        (list 'quote 'shen.functions)
        (list 'shen-port.primitives/defn-curried
              name
              (into [] vars)
              (kl->clj vars body))))

(defn undeclared [locals code]
  (->> code
       flatten
       (filter symbol?)
       (remove (fn [x] (some #{x} locals)))
       (remove resolve)
       distinct))

(defn auto-declare
  [locals code]
  (let [to-declare (undeclared (conj locals 'quote 'true 'false) code)]
    (if (not-empty to-declare)
      (list 'do (cons 'clojure.core/declare to-declare) code)
      code)))

(defn maybe-declare-fn
  [locals expr]
  (let [fname (first expr)]
    (if (and (symbol? fname)
             (not (boolean (some #{fname} locals)))
             (not (resolve fname)))
      (list 'do
            (list 'def fname)
            expr)
      expr)))

(defn member?
  [S X]
  (some #{X} S))

(defn kl->clj
  [locals expr]
  (cond

    (member? locals expr) expr

    ; Locals [type X _] -> (kl-to-lisp Locals X)
    (match? expr (['type _ _] :seq))
    (kl->clj locals (second expr))

    ; Locals [lambda X Y] -> (let ChX (ch-T X) (protect [FUNCTION [LAMBDA [ChX] (kl-to-lisp [ChX | Locals] (SUBST ChX X Y))]]))
    (match? expr (['lambda _ _] :seq))
    (let [[_ x y] expr]
      (declare-unresolvable-symbols locals (list 'clojure.core/fn [x] (kl->clj (conj locals x) y))))

    ; Locals [let X Y Z] -> (let ChX (ch-T X) (protect [LET [[ChX (kl-to-lisp Locals Y)]] (kl-to-lisp [ChX | Locals] (SUBST ChX X Z))]))
    (match? expr (['let _ _ _] :seq))
    (let [[_ x y z] expr]
      (list 'clojure.core/let [x (kl->clj locals y)] (kl->clj (conj locals x) z)))

    ; _ [defun F Locals Code] -> (protect [DEFUN F Locals (kl-to-lisp Locals Code)])
    (match? expr (['defun _ _ _] :seq))
    (let [[_ name vars body] expr]
      (if (< (count vars) 2)
        (uncurried-fn name vars body)
        (curried-fn name vars body)))

    ; Locals [cond | Cond] -> (protect [COND | (MAPCAR (/. C (cond_code Locals C)) Cond)])
    (and (seq? expr) (= (first expr) 'cond))
    (let [[_ & clauses] expr
          clauses' (concat clauses '((:else (throw (Exception. "No matching cond clause")))))
          clauses'' (mapcat (fn [[test body]]
                              (list (kl->clj locals test) (kl->clj locals body))) clauses')]
      (cons 'clojure.core/cond clauses''))

    (and (seq? expr) (= (first expr) 'do))
    (let [[_ & exprs] expr]
      (cons 'do (for [each exprs]
                  (kl->clj locals each))))

    (match? expr (['freeze _] :seq))
    (let [[_ x] expr]
      (list 'clojure.core/fn [] (kl->clj locals x)))

    (match? expr (['and _] :seq))
    (let [[_ x] expr]
      (list (internal-fn-symbol 'andp) (kl->clj locals x)))

    (match? expr (['and _ _] :seq))
    (let [[_ x y] expr]
      (list 'clojure.core/and (kl->clj locals x) (kl->clj locals y)))

    (match? expr (['or _] :seq))
    (let [[_ x] expr]
      (list (internal-fn-symbol 'orp) (kl->clj locals x)))

    (match? expr (['or _ _] :seq))
    (let [[_ x y] expr]
      (list 'clojure.core/or (kl->clj locals x) (kl->clj locals y)))

    (match? expr (['if _ _ _] :seq))
    (let [[_ x y z] expr]
      (list 'if (kl->clj locals x) (kl->clj locals y) (kl->clj locals z)))

    (match? expr (['if _] :seq))
    (let [[_ x] expr]
      (list (internal-fn-symbol 'ifp) (kl->clj locals x)))

    (match? expr (['if _ _] :seq))
    (let [[_ x y] expr]
      (list (internal-fn-symbol 'ifp) (kl->clj locals x) (kl->clj locals y)))

    (match? expr (['trap-error _ _] :seq))
    (let [[_ body handler] expr
          e (gensym "e")]
      (list 'try (kl->clj locals body) (list 'catch 'Exception e (list (kl->clj locals handler) e))))

    ; _ [] -> []
    (= '() expr)
    '()

    (seq? expr)
    (let [[fst & rest] expr
          fnexpr (cond (seq? fst)
                       (list 'shen.primitives/resolve-fn (kl->clj locals fst))

                       (member? locals fst)
                       (list 'shen.primitives/resolve-fn fst)

                       :else fst)]
      (cons fnexpr (for [arg rest]
                    (kl->clj locals arg))))

    ; _ S -> (protect [QUOTE S])  where (protect (= (SYMBOLP S) T))
    (symbol? expr)
    (list 'quote expr)

    :else expr))
