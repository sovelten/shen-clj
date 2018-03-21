(ns shen-port.backend-test
  (:require [shen-port.backend :as backend]
            [clojure.core.match :refer [match]]
            [midje.sweet :refer :all]))

(future-fact "innocent symbols"
       (fact (backend/kl->clj [] 'bla) => '(quote bla))
       (fact (eval (backend/kl->clj [] 'bla)) => 'bla))

(facts "Locals [type X _] -> (kl-to-lisp Locals X)"
       (fact (backend/kl->clj [] '(type x y)) => 'x))

(facts "Locals [lambda X Y] -> (let ChX (ch-T X) (protect [FUNCTION [LAMBDA [ChX] (kl-to-lisp [ChX | Locals] (SUBST ChX X Y))]]))"
       (fact (backend/kl->clj [] '(lambda x x)) => '(fn [x] x)))

(facts "(let x y z)"
       (fact (backend/kl->clj [] '(let x y z)) => '(let [x y] z)))

(facts "(defun name vars body)"
       (fact (backend/kl->clj [] '(defun func (x) x)) => '(defn func [x] x)))
