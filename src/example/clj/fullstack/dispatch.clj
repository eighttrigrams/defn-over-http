(ns fullstack.dispatch
  (:require [net.eighttrigrams.defn-over-http.core :refer [defdispatch defdispatch-with-args]]
            [fullstack.resources :refer [list-public-resources list-resources]]))

(defdispatch handler list-public-resources)

(defdispatch-with-args handler list-resources)