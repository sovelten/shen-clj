(ns shen-port.backend-test
  (:require [shen-port.backend :as backend]
            [clojure.core.match :refer [match]]
            [midje.sweet :refer :all]))

(facts "Locals [type X _] -> (kl-to-lisp Locals X)"
       (fact (backend/kl->clj [] '(type x y)) => '(quote x)))

(facts "Locals [lambda X Y] -> (let ChX (ch-T X) (protect [FUNCTION [LAMBDA [ChX] (kl-to-lisp [ChX | Locals] (SUBST ChX X Y))]]))"
       (fact (backend/kl->clj [] '(lambda x x)) => '(fn [x] x)))

(facts "(let x y z)"
       (fact (backend/kl->clj [] '(let x 5 z)) => '(let [x 5] (quote z))))

(facts "(defun name vars body)"
       (fact (backend/kl->clj [] '(defun func (x) x)) => '(shen.primitives/set* (quote func)
                                                                                (shen.primitives/curried-fn (quote ([x] x)))
                                                                                (quote shen.functions))))

(facts "empty list"
       (fact (backend/kl->clj [] '()) => '()))

(fact "innocent symbols"
      (fact (backend/kl->clj [] 'bla) => '(quote bla)))

;; WRAP

(facts "cons?"
       (fact (backend/wrap '(cons? X)) => '(boolean (not-empty X))))

(facts "and"
       (fact (backend/wrap '(and (cons? X) Y)) => '(and (boolean (not-empty X)) (shen-boolean Y))))

(facts "not"
       (fact (backend/wrap '(not X)) => '(not (shen-boolean X))))

(future-fact "="
       (fact (backend/wrap '(= X Y)) => '(not (shen-boolean X))))
