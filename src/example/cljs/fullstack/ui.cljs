(ns ^:figwheel-hooks fullstack.ui
  (:require
   [goog.dom :as gdom]
   [reagent.dom :as rdom]
   [fullstack.resources :as resources]))

(defn- resources []
  [:div
   [:div {:style {:position :absolute
                  :right "10px"
                  :top "8px"}}]
   [:h1 "Resources"]
   [resources/component]])

(defn get-app-element []
  (gdom/getElement "app"))
(defn mount [el]
  (rdom/render [resources] el))
(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)
(defn ^:after-load on-reload []
  (mount-app-element))
