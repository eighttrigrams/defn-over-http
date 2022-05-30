(ns fullstack.api
  (:require [ring.util.response :refer [response]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [compojure.core :as c]
            [compojure.route :as route]
            [fullstack.dispatch :as dispatch]))

(defn wrap-auth [handle]
  (fn [req]
    (handle (assoc-in req [:body :server-args :permissions] "all"))))

(c/defroutes query-route
  (-> #(response (dispatch/handler %))
      (wrap-auth)
      (wrap-json-response)
      (wrap-json-body {:keywords? true})))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(c/defroutes app
  (c/POST "/api" [] query-route)
  (route/resources "/"))