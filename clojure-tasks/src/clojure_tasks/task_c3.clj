(ns clojure-tasks.task-c3)

(defn parallel-filter
  ([pred coll]
   (parallel-filter pred coll 256 (.availableProcessors (Runtime/getRuntime))))
  ([pred coll block-size parallelism]
   (when-not (and (pos-int? block-size) (pos-int? parallelism))
     (throw (IllegalArgumentException. "block-size and parallelism must be positive integers")))
   (letfn [(process [blocks]
             (lazy-seq
              (when-let [blocks (seq blocks)]
                (let [batch (vec (take parallelism blocks))
                      jobs (mapv (fn [block]
                                   (future (into [] (filter pred) block)))
                                 batch)]
                  (concat (mapcat deref jobs)
                          (process (drop parallelism blocks)))))))]
     (process (partition-all block-size coll)))))
