(ns fullstack.api
  (:require-macros [net.eighttrigrams.defn-over-http.core :refer [defn-over-http]])
  (:require ajax.core))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(def config {:api-path "/api"
             :error-handler #(prn %)})

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn-over-http list-public-resources)

#_{:clj-kondo/ignore [:invalid-arity]}
(defn-over-http list-resources :return-value [])