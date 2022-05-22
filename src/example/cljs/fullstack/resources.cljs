(ns fullstack.resources
  (:require [reagent.core :as reagent :refer [atom]]
            [fullstack.api :as api]))

(defn- search-component [q on-change]
  [:div.search
   [:input
    {:type "text"
     :value @q
     :placeholder "search-term"
     :on-change (fn [change] (on-change (aget change "target" "value")))}]])

(defn- list-component [result]
  (fn []
    [:ul
     (doall (map-indexed (fn [index item] [:li {:key index} (:name item)])
                         @result))]))

(defn component []
  (let [query-string   (atom "")
        result         (atom [])
        handler        #(reset! result %)
        _error-handler #(prn "caught by error handler:" %)
        list-resources #((api/list-resources handler #_error-handler) @query-string)
        on-change      (fn [new-query-string]
                         (reset! query-string new-query-string)
                         (list-resources))]
    (fn []
      (list-resources)
      [:div
       [search-component query-string on-change]
       [list-component result]])))
