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
       (fact (p/eval-kl '((lambda X (+ X X)) 2)) => 4)
       #_(fact (prim/eval-kl '((lambda X (+ X X)) 2)) => 0))
