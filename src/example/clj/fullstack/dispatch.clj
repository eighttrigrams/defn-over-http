(ns fullstack.dispatch
  (:require [net.eighttrigrams.defn-over-http.core :refer [defdispatch]]
            [fullstack.resources :refer [list-resources]]))

(defdispatch handler list-resources)