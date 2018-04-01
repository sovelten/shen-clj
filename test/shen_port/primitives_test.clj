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
       (fact (p/eval-kl '(do (defun func (x) (+ x 2)) (func 5))) => 7)
       (fact (p/eval-kl '(defun func (x y) (+ x y))) => ifn?)
       (fact (p/eval-kl '(func 3 4)) => 7)
       (fact (p/eval-kl '(func 3)) => ifn?)
       (fact "currying" (p/eval-kl '((func 3) 4)) => 7))

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

(facts "Error Handling"
       (fact (p/eval-kl '(simple-error "My error")) => (throws "My error"))
       (fact (p/eval-kl '(trap-error (+ 2 3) (lambda E E))) => 5)
       (fact (p/eval-kl '(trap-error (simple-error "My error") (lambda E (error-to-string E)))) => "My error"))

(facts "Symbols"
       (fact (p/eval-kl '(intern "true")) => true)
       (fact (p/eval-kl '(intern "false")) => false)
       (fact (p/eval-kl '(symbol? (intern "true"))) => false))

(facts "Number Comparison"
       (fact (p/eval-kl '(number? 10)) => true)
       (fact (p/eval-kl '(number? banana)) => false)
       (fact (p/eval-kl '(> 6 5)) => true)
       (fact (p/eval-kl '(> 5 6)) => false)
       (fact (p/eval-kl '(>= 5 5)) => true)
       (fact (p/eval-kl '(< 5 5)) => false)
       (fact (p/eval-kl '(<= 5 5)) => true))

(facts "Strings"
       (fact (p/eval-kl '(string? "asdf")) => true)
       (fact (p/eval-kl '(cn "ba" "nana")) => "banana")
       (fact (p/eval-kl '(pos "abcd" 3)) => "d")
       (fact (p/eval-kl '(tlstr "abc")) => "bc")
       (fact (p/eval-kl '(str 256)) => "256")
       (fact (p/eval-kl '(string->n "b")) => 98)
       (fact (p/eval-kl '(string->n "bx")) => 98)
       (fact (p/eval-kl '(n->string 90)) => "Z"))

(facts "Vectors"
       (fact "create vector vec"
             (p/eval-kl '(absvector? (set vec (absvector 4)))) => true)
       (fact "assign a value to pos 0"
             (p/eval-kl '(address-> (value vec) 0 15)) => anything)
       (fact "get value back"
             (p/eval-kl '(<-address (value vec) 0)) => 15)
       (fact "vec is a vector"
             (p/eval-kl '(absvector? (value vec))) => true))

(facts "Conses"
       (fact (p/eval-kl '(cons? ())) => false)
       (fact (p/eval-kl '(cons? (cons 1))) => false)
       (fact (p/eval-kl '(cons? (cons 1 2))) => true)
       (fact (p/eval-kl '(cons? (cons 1 ()))) => true)
       (fact (p/eval-kl '(cons 1 2)) => '(1 2))
       (fact (p/eval-kl '(cons 1 ())) => '(1))
       (fact (p/eval-kl '(cons 1 (cons 2 ()))) => '(1 2))
       (fact (p/eval-kl '(cons 1 (cons 2 (cons 3 ())))) => '(1 2 3))
       (fact (p/eval-kl '(cons 1 (cons 2 3))) => '(1 (2 3)))
       (fact (p/eval-kl '(hd 1)) => (throws "Not a cons"))
       (fact (p/eval-kl '(hd (cons 1 ()))) => 1)
       (fact (p/eval-kl '(tl (cons 1 ()))) => '())
       (fact (p/eval-kl '(cons + (cons 1 (cons 2 ())))) => '(+ 1 2))
       (fact (p/eval-kl '(tl (cons 1 (cons 2 ())))) => '(2)))

(facts "Streams"
       (fact (p/eval-kl '(value *stinput*)) => *in*)
       (fact (p/eval-kl '(value *stoutput*)) => *out*))

(facts "do"
       (fact (p/eval-kl '(do (set myval 13) (value myval))) => 13))

(facts "Expected properties"
       (fact (p/eval-kl '(= "str" (cn "str" ""))) => true)
       (fact (p/eval-kl '(= "str" (cn "" "str"))) => true)
       (fact (p/eval-kl '(= "str" (cn (pos "str" 0) (tlstr "str")))) => true)
       (fact (p/eval-kl '(= (pos "str" 0) (n->string (string->n "str")))) => true)
       (fact (p/eval-kl '(= (intern "str") (intern "str"))) => true)
       (fact (p/eval-kl '(do (set x y) (= (value x) y))) => true)
       (fact (p/eval-kl '(= (intern "str") (intern "str"))) => true)
       (fact (p/eval-kl '(= "error" (trap-error (simple-error "error") (lambda E (error-to-string E))))) => true)
       (fact (p/eval-kl '(= "foo" (hd (cons "foo" "bar")))) => true)
       (fact (p/eval-kl '(= "bar" (tl (cons "foo" "bar")))) => true)
       (fact (p/eval-kl '(= 3 (eval-kl (cons + (cons 1 (cons 2 ())))))) => true)
       (fact (p/eval-kl '(= (+ 1 2) ((+ 1) 2))) => true)
       (fact (p/eval-kl '(boolean? true)) => true)
       (fact (p/eval-kl '(boolean? false)) => true)
       (fact (p/eval-kl '(boolean? (intern "true"))) => true)
       (fact (p/eval-kl '(boolean? (intern "false"))) => true)

       (fact (p/eval-kl '(symbol? true)) => false)
       (fact (p/eval-kl '(symbol? false)) => false)
       (fact (p/eval-kl '(symbol? (intern "true"))) => false)
       (fact (p/eval-kl '(symbol? (intern "false"))) => false)
       (fact (p/eval-kl '(symbol? (lambda X X))) => false)
       (fact (p/eval-kl '(symbol? (value *stinput*))) => false)
       (fact (p/eval-kl '(trap-error (simple-error "") (lambda E (symbol? E)))) => false)
       (fact (p/eval-kl '(symbol? ())) => false))

(facts "time"
       (fact (p/eval-kl '(get-time run)) => number?)
       (fact (p/eval-kl '(get-time unix)) => number?))

(def code-sample '(defun shen.find (V3990 V3991) (cond ((= () V3991) ()) ((and (cons? V3991) (V3990 (hd V3991))) (cons (hd V3991) (shen.find V3990 (tl V3991)))) ((cons? V3991) (shen.find V3990 (tl V3991))) (true (shen.f_error shen.find)))))


#_(facts ""
       (fact (p/eval-kl code-sample) => nil))
