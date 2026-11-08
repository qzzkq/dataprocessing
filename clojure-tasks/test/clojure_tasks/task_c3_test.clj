(ns clojure-tasks.task-c3-test
  (:require [clojure.test :refer [deftest is]]
            [clojure-tasks.task-c3 :as c3])
  (:import [java.util.concurrent CountDownLatch TimeUnit]))

(deftest finite-input-test
  (is (= [] (c3/parallel-filter odd? [] 3 2)))
  (is (= (filter odd? (range 101))
         (c3/parallel-filter odd? (range 101) 7 3)))
  (is (= [] (c3/parallel-filter (constantly false) (range 20) 3 2)))
  (is (thrown? IllegalArgumentException (c3/parallel-filter odd? [1] 0 2)))
  (is (thrown? IllegalArgumentException (c3/parallel-filter odd? [1] 2 0))))

(deftest infinite-input-test
  (is (= (range 0 40 2)
         (take 20 (c3/parallel-filter even? (iterate inc 0) 4 3)))))

(deftest bounded-laziness-test
  (let [seen (atom 0)
        input (map (fn [x] (swap! seen inc) x) (iterate inc 0))
        result (c3/parallel-filter even? input 4 2)]
    (is (zero? @seen))
    (is (= 0 (first result)))
    (is (<= @seen 12))))

(deftest futures-run-in-parallel-test
  (let [started (CountDownLatch. 2)
        pred (fn [x]
               (when (even? x)
                 (.countDown started)
                 (when-not (.await started 2 TimeUnit/SECONDS)
                   (throw (ex-info "Blocks did not run in parallel" {}))))
               true)]
    (is (= [0 1 2 3] (c3/parallel-filter pred (range 4) 2 2)))))
