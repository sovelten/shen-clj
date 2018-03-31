(ns shen-port.kl-reader-test
  (:require [shen-port.kl-reader :as kl-reader]
            [midje.sweet :refer :all]))

(facts "parser"
       (fact (kl-reader/parser "-1") => [:klexpr [:number "-1"]])
       (fact (kl-reader/parser "-1.35") => [:klexpr [:number "-1.35"]])
       (fact (kl-reader/parser "15") => [:klexpr [:number "15"]])
       (fact (kl-reader/parser "25.13") => [:klexpr [:number "25.13"]])
       (fact (kl-reader/parser "+25.13") => [:klexpr [:symbol "+25.13"]])

       (fact (kl-reader/parser "\"\"") => [:klexpr [:string ""]])
       (fact (kl-reader/parser "\"anything\"") => [:klexpr [:string "anything"]])
       (fact (kl-reader/parser "\"any\nthing\"") => [:klexpr [:string "any\nthing"]])
       (fact (kl-reader/parser "\"any\nthing\"") => [:klexpr [:string "any\nthing"]])
       (fact (kl-reader/parser "\"any\"thing\"") => (contains {:text "\"any\"thing\""}))

       (fact (kl-reader/parser "bla") => [:klexpr [:symbol "bla"]])
       (fact (kl-reader/parser "V12345") => [:klexpr [:symbol "V12345"]])
       (fact (kl-reader/parser "-") => [:klexpr [:symbol "-"]])
       (fact (kl-reader/parser "-->") => [:klexpr [:symbol "-->"]])
       (fact (kl-reader/parser "*") => [:klexpr [:symbol "*"]])

       (fact (kl-reader/parser "()") => [:klexpr [:list]])
       (fact (kl-reader/parser "(25)") => [:klexpr [:list [:number "25"]]])
       (fact (kl-reader/parser "(bla 25 \"string\")")
             => [:klexpr [:list [:symbol "bla"]
                                [:number "25"]
                                [:string "string"]]]))

(facts "read"
       (fact (kl-reader/read "25") => 25)
       (fact (kl-reader/read "bla") => 'bla)
       (fact (kl-reader/read "\"bla\"") => "bla")
       (fact (kl-reader/read "(+ 1 2)") => '(+ 1 2))
       (fact (kl-reader/read "(+ 1 2 \"bla\" (s a b c))")
             => '(+ 1 2 "bla" (s a b c))))

(facts "ignore new lines"
       (fact (kl-reader/read "25") => 25)
       (fact (kl-reader/read "bla") => 'bla)
       (fact (kl-reader/read "\"bla\"") => "bla")
       (fact (kl-reader/read "(\n+ 1\n 2\n)") => '(+ 1 2))
       (fact (kl-reader/read "\n(+ 1 2 \"bla\" \n(s a b c))")
             => '(+ 1 2 "bla" (s a b c))))

(facts "file parser"
       (fact (kl-reader/file-parser "\n\n(+ 1 2)\n\n (+ 4 5)\n")
             => [:file
                 [:klexpr [:list [:symbol "+"][:number "1"] [:number "2"]]]
                 [:klexpr [:list [:symbol "+"][:number "4"] [:number "5"]]]]))

(facts "read-file"
       (fact (kl-reader/read-file "\n\n(+ 1 2)\n\n (+ 4 5)\n")
             => '((+ 1 2) (+ 4 5))))

(def sample "(defun shen.curry-type (V1397) (cond ((and (cons? V1397) (and (cons? (tl V1397)) (and (= --> (hd (tl V1397))) (and (cons? (tl (tl V1397))) (and (cons? (tl (tl (tl V1397)))) (= --> (hd (tl (tl (tl V1397)))))))))) (shen.curry-type (cons (hd V1397) (cons --> (cons (tl (tl V1397)) ()))))) ((and (cons? V1397) (and (cons? (tl V1397)) (and (= * (hd (tl V1397))) (and (cons? (tl (tl V1397))) (and (cons? (tl (tl (tl V1397)))) (= * (hd (tl (tl (tl V1397)))))))))) (shen.curry-type (cons (hd V1397) (cons * (cons (tl (tl V1397)) ()))))) ((cons? V1397) (map (lambda Z (shen.curry-type Z)) V1397)) (true V1397)))")

(facts "sample"
       (fact (kl-reader/parser sample) => anything))

