#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(ns fullstack.api
  (:require-macros [net.eighttrigrams.defn-over-http.core :refer [defn-over-http]])
  (:require ajax.core
            cognitect.transit))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(def api-path "/api")

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(def base-error-handler #(prn "error caught by base error handler:" %))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn fetch-base-headers [] {})

(defn-over-http list-resources [])