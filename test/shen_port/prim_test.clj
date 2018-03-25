(ns shen-port.prim-test
  (:require [shen-port.prim :as prim]
            [midje.sweet :refer :all]))

(prim/fn-curried [x y] (+ x y))
