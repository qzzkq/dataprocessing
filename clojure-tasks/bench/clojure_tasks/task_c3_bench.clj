(ns clojure-tasks.task-c3-bench
  (:require [clojure-tasks.task-c3 :as c3]))

(defn- slow-even? [n]
  (Thread/sleep 10)
  (even? n))

(defn -main [& _]
  (let [input (range 80)]
    (println "filter")
    (time (doall (filter slow-even? input)))
    (println "parallel-filter")
    (time (doall (c3/parallel-filter slow-even? input 5 4))))
  (shutdown-agents))
