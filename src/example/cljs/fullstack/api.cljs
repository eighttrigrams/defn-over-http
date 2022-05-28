#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(ns fullstack.api
  (:require-macros [net.eighttrigrams.defn-over-http.core :refer [defn-over-http]])
  (:require ajax.core))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(def api-path "/api")

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn fetch-base-headers [] {})

(def error-handler #(prn "error caught by base error handler:" %))

(defn-over-http list-resources error-handler)