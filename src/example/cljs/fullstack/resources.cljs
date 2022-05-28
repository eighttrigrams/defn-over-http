(ns fullstack.resources
  (:require [reagent.core :as reagent :refer [atom]]
            [fullstack.api :as api]
            [cljs.core.async :refer [go]]
            [cljs.core.async.interop :refer-macros [<p!]]))

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
        list-resources #(go 
                          (try (->> @query-string
                                    api/list-resources
                                    <p!
                                    (reset! result))
                               (catch js/Error err (prn (.-cause err)))))
        on-change      (fn [new-query-string]
                         (reset! query-string new-query-string)
                         (list-resources))]
    (fn []
      (list-resources)
      [:div
       [search-component query-string on-change]
       [list-component result]])))
