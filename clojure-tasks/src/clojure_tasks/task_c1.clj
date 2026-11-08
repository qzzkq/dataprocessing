(ns clojure-tasks.task-c1)

(defn strings [alphabet n]
  (reduce (fn [prefixes _]
            (reduce (fn [result prefix]
                      (into result
                            (map #(str prefix %)
                                 (remove #(= % (str (last prefix))) alphabet))))
                    []
                    prefixes))
          [""]
          (char-array n)))
