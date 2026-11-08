(ns clojure-tasks.test-runner
  (:require [clojure.test :as test]
            [clojure-tasks.task-c2-test]
            [clojure-tasks.task-c3-test]))

(defn -main [& _]
  (let [result (test/run-tests 'clojure-tasks.task-c2-test
                               'clojure-tasks.task-c3-test)]
    (shutdown-agents)
    (when (pos? (+ (:fail result) (:error result)))
      (System/exit 1))))
