(ns clojure-tasks.task-c2)

(defn- sieve [candidate composites]
  (lazy-seq
   (loop [n candidate
          table composites]
     (if-let [steps (get table n)]
       (recur (inc' n)
              (reduce (fn [next-table step]
                        (update next-table (+' n step) (fnil conj []) step))
                      (dissoc table n)
                      steps))
       (cons n (sieve (inc' n) (assoc table (*' n n) [n])))))))

(def primes (sieve 2 {}))
