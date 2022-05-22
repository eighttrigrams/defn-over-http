(ns fullstack.resources
  (:require [clojure.string :as str]))

(def resources
  [{:id 1 :name "one"}
   {:id 2 :name "two"}
   {:id 3 :name "three" :protected true}])

(defn list-resources [{permissions :permissions}] 
  (fn [query-string]
    #_(throw (Exception. "oh no"))
    (->> resources
         (remove #(when (not= permissions "all")
                    (:protected %)))
         (remove #(not (str/starts-with? (:name %) query-string))))))
