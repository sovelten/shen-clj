(ns shen-port.overwrite
  (:require [shen-port.primitives :refer :all])
  (:refer-clojure :only []))

;;
;; Replace for function with tail call optimization (until we support tail call optimization)
;;

(def shen-dot-fillvector
  '(shen-port.primitives/with-ns 'shen.functions
     (clojure.core/defn shen-dot-fillvector
       ([Vec pos c d]
        (clojure.core/cond (= c pos) (address-> Vec c d)
                           :else   (recur (address-> Vec pos d) (+ 1 pos) c d)))
       ([Vec pos c]
        (clojure.core/partial shen-dot-fillvector Vec pos c))
       ([Vec pos]
        (clojure.core/partial shen-dot-fillvector Vec pos))
       ([Vec]
        (clojure.core/partial shen-dot-fillvector Vec)))))

;;
;; Functions that exceed 64KB of code mush be broken down due to java method limit
;;

(def shen-dot-aum_to_shen-aux
  '(shen-port.primitives/with-ns (quote shen.functions) (clojure.core/defn first-cond [V2046] (clojure.core/and (cons? V2046) (clojure.core/and (= (quote let) (hd V2046)) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-be) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl (tl V2046))))) (clojure.core/and (= (quote in) (hd (tl (tl (tl (tl V2046)))))) (clojure.core/and (cons? (tl (tl (tl (tl (tl V2046)))))) (= () (tl (tl (tl (tl (tl (tl V2046)))))))))))))))))))

(def shen-dot-aum_to_shen
  '(shen-port.primitives/with-ns (quote shen.functions) (clojure.core/defn shen-dot-aum_to_shen [V2046] (clojure.core/cond (first-cond V2046) (cons (quote let) (cons (hd (tl V2046)) (cons (shen-dot-aum_to_shen (hd (tl (tl (tl V2046))))) (cons (shen-dot-aum_to_shen (hd (tl (tl (tl (tl (tl V2046))))))) ())))) (clojure.core/and (cons? V2046) (clojure.core/and (= (quote shen-dot-the) (hd V2046)) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (= (quote shen-dot-result) (hd (tl V2046))) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-of) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (clojure.core/and (= (quote shen-dot-dereferencing) (hd (tl (tl (tl V2046))))) (clojure.core/and (cons? (tl (tl (tl (tl V2046))))) (= () (tl (tl (tl (tl (tl V2046))))))))))))))) (cons (quote shen-dot-lazyderef) (cons (shen-dot-aum_to_shen (hd (tl (tl (tl (tl V2046)))))) (cons (quote ProcessN) ()))) (clojure.core/and (cons? V2046) (clojure.core/and (= (quote if) (hd V2046)) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-then) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl (tl V2046))))) (clojure.core/and (= (quote shen-dot-else) (hd (tl (tl (tl (tl V2046)))))) (clojure.core/and (cons? (tl (tl (tl (tl (tl V2046)))))) (= () (tl (tl (tl (tl (tl (tl V2046)))))))))))))))) (cons (quote if) (cons (shen-dot-aum_to_shen (hd (tl V2046))) (cons (shen-dot-aum_to_shen (hd (tl (tl (tl V2046))))) (cons (shen-dot-aum_to_shen (hd (tl (tl (tl (tl (tl V2046))))))) ())))) (clojure.core/and (cons? V2046) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (= (quote is) (hd (tl V2046))) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-a) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (clojure.core/and (= (quote shen-dot-variable) (hd (tl (tl (tl V2046))))) (= () (tl (tl (tl (tl V2046)))))))))))) (cons (quote shen-dot-pvar?) (cons (hd V2046) ())) (clojure.core/and (cons? V2046) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (= (quote is) (hd (tl V2046))) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-a) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (clojure.core/and (= (quote shen-dot-non-empty) (hd (tl (tl (tl V2046))))) (clojure.core/and (cons? (tl (tl (tl (tl V2046))))) (clojure.core/and (= (quote list) (hd (tl (tl (tl (tl V2046)))))) (= () (tl (tl (tl (tl (tl V2046))))))))))))))) (cons (quote cons?) (cons (hd V2046) ())) (clojure.core/and (cons? V2046) (clojure.core/and (= (quote shen-dot-rename) (hd V2046)) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (= (quote shen-dot-the) (hd (tl V2046))) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-variables) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (clojure.core/and (= (quote in) (hd (tl (tl (tl V2046))))) (clojure.core/and (cons? (tl (tl (tl (tl V2046))))) (clojure.core/and (= () (hd (tl (tl (tl (tl V2046)))))) (clojure.core/and (cons? (tl (tl (tl (tl (tl V2046)))))) (clojure.core/and (= (quote and) (hd (tl (tl (tl (tl (tl V2046))))))) (clojure.core/and (cons? (tl (tl (tl (tl (tl (tl V2046))))))) (clojure.core/and (= (quote shen-dot-then) (hd (tl (tl (tl (tl (tl (tl V2046)))))))) (clojure.core/and (cons? (tl (tl (tl (tl (tl (tl (tl V2046)))))))) (= () (tl (tl (tl (tl (tl (tl (tl (tl V2046)))))))))))))))))))))))) (shen-dot-aum_to_shen (hd (tl (tl (tl (tl (tl (tl (tl V2046))))))))) (clojure.core/and (cons? V2046) (clojure.core/and (= (quote shen-dot-rename) (hd V2046)) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (= (quote shen-dot-the) (hd (tl V2046))) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-variables) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (clojure.core/and (= (quote in) (hd (tl (tl (tl V2046))))) (clojure.core/and (cons? (tl (tl (tl (tl V2046))))) (clojure.core/and (cons? (hd (tl (tl (tl (tl V2046)))))) (clojure.core/and (cons? (tl (tl (tl (tl (tl V2046)))))) (clojure.core/and (= (quote and) (hd (tl (tl (tl (tl (tl V2046))))))) (clojure.core/and (cons? (tl (tl (tl (tl (tl (tl V2046))))))) (clojure.core/and (= (quote shen-dot-then) (hd (tl (tl (tl (tl (tl (tl V2046)))))))) (clojure.core/and (cons? (tl (tl (tl (tl (tl (tl (tl V2046)))))))) (= () (tl (tl (tl (tl (tl (tl (tl (tl V2046)))))))))))))))))))))))) (cons (quote let) (cons (hd (hd (tl (tl (tl (tl V2046)))))) (cons (cons (quote shen-dot-newpv) (cons (quote ProcessN) ())) (cons (shen-dot-aum_to_shen (cons (quote shen-dot-rename) (cons (quote shen-dot-the) (cons (quote shen-dot-variables) (cons (quote in) (cons (tl (hd (tl (tl (tl (tl V2046)))))) (tl (tl (tl (tl (tl V2046))))))))))) ())))) (clojure.core/and (cons? V2046) (clojure.core/and (= (quote bind) (hd V2046)) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-to) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl (tl V2046))))) (clojure.core/and (= (quote in) (hd (tl (tl (tl (tl V2046)))))) (clojure.core/and (cons? (tl (tl (tl (tl (tl V2046)))))) (= () (tl (tl (tl (tl (tl (tl V2046)))))))))))))))) (cons (quote do) (cons (cons (quote shen-dot-bindv) (cons (hd (tl V2046)) (cons (shen-dot-chwild (hd (tl (tl (tl V2046))))) (cons (quote ProcessN) ())))) (cons (cons (quote let) (cons (quote Result) (cons (shen-dot-aum_to_shen (hd (tl (tl (tl (tl (tl V2046))))))) (cons (cons (quote do) (cons (cons (quote shen-dot-unbindv) (cons (hd (tl V2046)) (cons (quote ProcessN) ()))) (cons (quote Result) ()))) ())))) ()))) (clojure.core/and (cons? V2046) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (= (quote is) (hd (tl V2046))) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote identical) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (clojure.core/and (= (quote shen-dot-to) (hd (tl (tl (tl V2046))))) (clojure.core/and (cons? (tl (tl (tl (tl V2046))))) (= () (tl (tl (tl (tl (tl V2046)))))))))))))) (cons (quote =) (cons (hd (tl (tl (tl (tl V2046))))) (cons (hd V2046) ()))) (= (quote shen-dot-failed!) V2046) (quote false) (clojure.core/and (cons? V2046) (clojure.core/and (= (quote shen-dot-the) (hd V2046)) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (= (quote head) (hd (tl V2046))) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-of) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (= () (tl (tl (tl (tl V2046)))))))))))) (cons (quote hd) (tl (tl (tl V2046)))) (clojure.core/and (cons? V2046) (clojure.core/and (= (quote shen-dot-the) (hd V2046)) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (= (quote tail) (hd (tl V2046))) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-of) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (= () (tl (tl (tl (tl V2046)))))))))))) (cons (quote tl) (tl (tl (tl V2046)))) (clojure.core/and (cons? V2046) (clojure.core/and (= (quote shen-dot-pop) (hd V2046)) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (= (quote shen-dot-the) (hd (tl V2046))) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-stack) (hd (tl (tl V2046)))) (= () (tl (tl (tl V2046)))))))))) (cons (quote do) (cons (cons (quote shen-dot-incinfs) ()) (cons (cons (quote thaw) (cons (quote Continuation) ())) ()))) (clojure.core/and (cons? V2046) (clojure.core/and (= (quote call) (hd V2046)) (clojure.core/and (cons? (tl V2046)) (clojure.core/and (= (quote shen-dot-the) (hd (tl V2046))) (clojure.core/and (cons? (tl (tl V2046))) (clojure.core/and (= (quote shen-dot-continuation) (hd (tl (tl V2046)))) (clojure.core/and (cons? (tl (tl (tl V2046)))) (= () (tl (tl (tl (tl V2046)))))))))))) (cons (quote do) (cons (cons (quote shen-dot-incinfs) ()) (cons (shen-dot-call_the_continuation (shen-dot-chwild (hd (tl (tl (tl V2046))))) (quote ProcessN) (quote Continuation)) ()))) (quote true) V2046 :else "TODO"))))

(def shen-dot-th*
  '(shen-port.primitives/with-ns
     (quote shen.functions)
     (shen-port.primitives/defn-curried shen-dot-th*
       [V1 V2 V3 V4 V5]
       (def ^:dynamic V3747)
       (def ^:dynamic V3748)
       (def ^:dynamic V3749)
       (def ^:dynamic V3750)
       (def ^:dynamic V3751)
       (clojure.core/binding [V3747 V1
                              V3748 V2
                              V3749 V3
                              V3750 V4
                              V3751 V5]
         (clojure.core/eval (clojure.core/read-string (clojure.core/slurp "resources/shen-dot-th*")))))))

(def shen-dot-t*-hyps
  '(shen-port.primitives/with-ns
     (quote shen.functions)
     (shen-port.primitives/defn-curried shen-dot-t*-hyps
       [V1 V2 V3 V4]
       (def ^:dynamic V3756)
       (def ^:dynamic V3757)
       (def ^:dynamic V3758)
       (def ^:dynamic V3759)
       (clojure.core/binding [V3756 V1
                              V3757 V2
                              V3758 V3
                              V3759 V4]
         (clojure.core/eval (clojure.core/read-string (clojure.core/slurp "resources/shen-dot-t*-hyps")))))))
