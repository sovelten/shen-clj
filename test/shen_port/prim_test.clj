(ns shen-port.prim-test
  (:require [shen-port.prim :as prim]
            [midje.sweet :refer :all]))

(facts "self evaluate symbols"
       (fact (prim/eval-kl 'bla) => 'bla)
       (fact (prim/eval-kl-2 'bla) => 'bla))

(facts "self evaluate numbers"
       (fact (prim/eval-kl '1) => 1)
       (fact (prim/eval-kl-2 '1) => 1))

(facts "self evaluate booleans"
       (fact (prim/eval-kl 'true) => true)
       (fact (prim/eval-kl 'false) => false)
       (fact (prim/eval-kl-2 'true) => true)
       (fact (prim/eval-kl-2 'false) => false))

(facts "self evaluate empty list"
       (fact (prim/eval-kl '()) => ())
       (fact (prim/eval-kl-2 '()) => ()))

(fact "set and value"
      (prim/eval-kl '(set my-val 2)) => 2
      (prim/eval-kl '(value my-val)) => 2
      (prim/eval-kl '(set my-val 15)) => 15
      (prim/eval-kl '(value my-val)) => 15)

(facts "on lambda"
       (fact ((prim/lambda X (+ X X)) 2) => 4)
       #_(fact ((prim/lambda2 X (+ X X)) 2) => 4)
       (fact (prim/eval-kl '(+ 1 3)) => 4)
       (fact (macroexpand '(prim/lambda X (+ X X))) => '(fn* ([X] (+ X X))))
       #_(fact (let [my-fn (prim/eval-kl-2 '(lambda X (+ X X)))]
               (my-fn 2) => 4))
       #_(fact (prim/eval-kl-2 '((lambda X (+ X X)) 2)) => nil)
       #_(fact (prim/eval-kl '((lambda X (+ X X)) 2)) => 0))



