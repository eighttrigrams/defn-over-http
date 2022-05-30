(ns net.eighttrigrams.defn-over-http.core)

(defmacro defn-over-http
  ([fname]
   `(defn-over-http ~fname ['nil 'nil]))
  ([fname [return-value base-error-handler]]
   `(defn-over-http ~fname [~return-value ~base-error-handler] ~(symbol 'fetch-base-headers)))
  ([fname [return-value base-error-handler] fetch-base-headers]
   (let [args          (gensym)
         handle-error  (gensym)
         request       (gensym)
         reader        (gensym)
         writer        (gensym)
         resolve       (gensym)
         reject        (gensym)
         e             (gensym)
         e-type        (gensym)
         e1            (gensym)
         return        (gensym)
         thrown        (gensym)]
     `(do
        (require 'cognitect.transit)
        (defn ~fname [& ~args]
          (js/Promise.
           (fn [~resolve ~reject]
             (let [~reader       (cognitect.transit/reader :json) ;; TODO close?
                   ~writer       (cognitect.transit/writer :json)
                   ~handle-error (fn [~e-type ~e]
                                   (if ~base-error-handler
                                     (do (~base-error-handler {:reason ~e-type :msg ~e})
                                         (~resolve ~return-value))
                                     (do
                                       (~reject {:reason ~e-type :msg ~e}))))
                   ~request      {:response-format :json
                                  :keywords?       true
                                  :headers         (merge
                                                    (~fetch-base-headers)
                                                    {"Content-Type" "application/json"})
                                  :body            (.stringify
                                                    js/JSON
                                                    (cljs.core/clj->js
                                                     {:fn   ~(str fname)
                                                      :args (cognitect.transit/write ~writer ~args)}))
                                  :error-handler   (fn [~e1]
                                                     (if
                                                      (map? ~e1)
                                                       (if (= (:failure ~e1) :parse)
                                                         (~handle-error :malformed-json-body
                                                                        (str "Expected json body but got: '" (:original-text ~e1)
                                                                             "'. Problem description: '" (:status-text ~e1) "'"))
                                                         (if (= 0 (:status ~e1))
                                                           (~handle-error :backend-not-reachable "(env: prod) Backend not reachable")
                                                           (~handle-error :http-error [(:status ~e1) (:status-text ~e1)])))
                                                       (~handle-error :unknown         ~e1)))
                                  :handler         (fn [{~return :return
                                                         ~thrown :thrown}]
                                                     (if ~thrown
                                                       (~handle-error :exception ~thrown)
                                                       (~resolve (cognitect.transit/read ~reader ~return))))}]
               (ajax.core/POST ~(symbol 'api-path) ~request)))))))))

(defmacro ^:private -defdispatch [fname pass-server-args & names]
  (let [function    (gensym)
        args        (gensym)
        server-args (gensym)
        writer      (gensym)
        reader      (gensym)
        os          (gensym)
        is          (gensym)
        e           (gensym)]
    `(do
       (require 'cognitect.transit)
       (defn ~fname
        [{{~function    :fn
           ~args        :args
           ~server-args :server-args} :body}]
        (let [~os     (java.io.ByteArrayOutputStream. 4096)
              ~is     (java.io.ByteArrayInputStream. (.getBytes ~args))
              ~writer (cognitect.transit/writer ~os :json)
              ~reader (cognitect.transit/reader ~is :json)]
          (try (case ~function
                 ~@(mapcat (fn [name]
                             [(str name) `{:return (do
                                                     (cognitect.transit/write
                                                      ~writer
                                                      (apply (if ~pass-server-args
                                                               (~name ~server-args)
                                                               ~name)
                                                             (cognitect.transit/read ~reader)))
                                                     (.toString ~os))
                                           :thrown nil}]) names)
                 {:return nil
                  :thrown (str "Unknown function: '" ~function "'")})
               (catch Exception ~e
                 {:return nil
                  :thrown (.toString ~e)})
               (finally (.close ~os)
                        (.close ~is))))))))

(defmacro defdispatch [fname & args]
  (apply (partial #'-defdispatch &form &env fname false) args))

(defmacro defdispatch-with-args [fname & args]
  (apply (partial #'-defdispatch &form &env fname true) args))
