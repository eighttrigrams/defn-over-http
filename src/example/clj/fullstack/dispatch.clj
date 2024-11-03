(ns fullstack.dispatch
  (:require [net.eighttrigrams.defn-over-http.core :refer [defdispatch]]
            [fullstack.resources :refer [#_list-public-resources list-resources]]))

(defn handle-error [e]
  (.printStackTrace e))

(defdispatch handler {:error-handler handle-error
                      :pass-server-args? true} list-resources)
