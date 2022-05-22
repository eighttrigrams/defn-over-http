(ns fullstack.proxy
  (:require [compojure.route :as route]
            [derekchiang.ring-proxy :as proxy]))

(defn wrap-fallback-exception
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception _e
        ;; We want to make no assumptions about the context
        ;; in which defn-over-http gets developed.
        ;; So the code in the macros must not know about this proxy.
        ;; Then we resort simply to the general mechanism of
        ;; simulating a runtime exception as would be thrown
        ;; should a developer introduce an error. Therefore the 200.
        {:status 200 :body "{\"return\": null, \"thrown\": \"(env: dev) Proxy says: backend not reachable\"}"}))))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(def handler
  (-> (route/not-found "Page not found")
      (proxy/wrap-proxy "/api" "http://0.0.0.0:3000/api")
      (wrap-fallback-exception)))
