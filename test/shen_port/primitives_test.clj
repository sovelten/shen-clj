(ns shen-port.primitives-test
  (:require [shen-port.primitives :as p]
            [midje.sweet :refer :all]))

(facts "set and value"
       (fact (p/eval-kl '(set my-val 2)) => 2)
       (fact (p/eval-kl '(value my-val)) => 2)
       (fact (p/eval-kl '(set my-val 15)) => 15)
       (fact (p/eval-kl '(value my-val)) => 15))

(facts "self evaluate symbols"
       (fact (p/eval-kl 'bla) => 'bla))

(facts "self evaluate numbers"
       (fact (p/eval-kl '1) => 1))

(facts "self evaluate booleans"
       (fact (p/eval-kl 'true) => true)
       (fact (p/eval-kl 'false) => false))

(facts "self evaluate empty list"
       (fact (p/eval-kl '()) => ()))

(facts "on lambda"
       (fact (p/eval-kl '((lambda X (+ X X)) 2)) => 4))

(facts "on defun"
       (fact (p/eval-kl '(defun func (x y) (+ x y))) => ifn?)
       (fact (p/eval-kl '(func 3 4)) => 7)
       (fact (p/eval-kl '(func 3)) => ifn?)
       (fact "currying"
             (p/eval-kl '((func 3) 4)) => 7))

(facts "on let"
       (fact (p/eval-kl '(let X 4 (+ X 2))) => 6))

(facts "arithmetic"
       (fact "+" (p/eval-kl '(+ 2 4)) => 6)
       (fact "+-curried" (p/eval-kl '((+ 1) 4)) => 5)
       (fact "-" (p/eval-kl '(- 10 5)) => 5)
       (fact "/" (p/eval-kl '(/ 10 5)) => 2))

(facts "freeze"
       (fact (p/eval-kl '(freeze (+ 2 3))) => ifn?)
       (fact (p/eval-kl '((freeze (+ 2 3)))) => 5))

(facts "conditionals"
       ;TODO: Should fail when expressions are not boolean
       (fact (p/eval-kl '(and true false)) => false)
       (fact (p/eval-kl '((and true) false)) => false)
       (fact (p/eval-kl '(or true false)) => true)
       (fact (p/eval-kl '((or true) false)) => true)
       (fact "we short circuit conditional"
             (p/eval-kl '(set my-val 0)) => 0
             (p/eval-kl '(and false (set my-val 4))) => false
             (p/eval-kl '(value my-val)) => 0)
       (fact (p/eval-kl '(if true 5 2)) => 5)
       (fact (p/eval-kl '((if false) 5 2)) => 2)
       (fact (p/eval-kl '((if false 5) 2)) => 2))

(facts "cond"
       ;TODO: Throw exception for default and don't accept truthy values
       (fact (p/eval-kl '(cond (true 4))) => 4)
       (fact (p/eval-kl '(cond (false 4)
                               (true 5))) => 5)
       (fact (p/eval-kl '(cond (false 4)
                               (false 5))) => "TODO"))
