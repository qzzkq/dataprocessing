(ns clojure-tasks.task-c2-test
  (:require [clojure.test :refer [deftest is]]
            [clojure-tasks.task-c2 :as c2]))

(deftest sieve-test
  (is (= [2 3 5 7 11 13 17 19 23 29 31 37 41 43 47]
         (take 15 c2/primes)))
  (is (= 541 (nth c2/primes 99)))
  (is (= 7919 (nth c2/primes 999)))
  (is (= [97 101 103 107 109 113]
         (take 6 (drop-while #(< % 97) c2/primes)))))
