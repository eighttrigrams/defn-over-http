# defn-over-http
 
A small library that allows users to call *Clojure* backend 
functions from *ClojureScript* frontend code. Arguments and return values
are passed back and forth via the [transit-format](https://github.com/cognitect/transit-format).

## Usage

To fetch the dependency via Leiningen use `[net.eighttrigrams/defn-over-http "0.1.10"]`.

## Rationale

Assuming an webapp setup where you have a frontend, say a single
page application, and a backend API, which is primarily made to support
ajax calls from the frontend, to supply it with data.

Now, let's say on the backend you have a function. Furthermore let's say you want to call this function from the frontend, with the least amount of extra code to organize the communication between frontend and backend.

What if all it would take would be some sort of declaration that such a function 
exists and should be wired up? It then could approximate something like a regular function call, only that it is across the wire.

## Walkthrough

Note that an [example application](./README.md#example-application) is part of this repository. 

Let `list-resources` be our function on the backend:

```clojure
(ns fullstack.resources)
(defn list-resources 
  [{}] ;; <- see section "# Server Arguments" below
  (fn [query-string] ...) ;; <- returns a vector
```

To declare it callable, all that is necessary is something like this:

```clojure
(ns fullstack.dispatch
 (:require 
  [net.eighttrigrams.defn-over-http.core :refer [defdispatch]]
  ;; Refer to the original function
  [fullstack.resources :refer [list-resources]])) 
    
;; Declare the function as api callable
(defdispatch handler list-resources)
```

To declare this same function on the frontend you'll have to set up something like
this:

```clojure
(ns fullstack.api
 (:require-macros [net.eighttrigrams.defn-over-http.core :refer [defn-over-http]])
 (:require ajax.core))

(def api-path "/api")
;; See section "# Configuration"
(defn fetch-base-headers [] {})
(def base-error-handler nil) 

;; This line will "create" and make available 
;; on the frontend a function 
;; with the corresponding (!) name.
;; [] will be the return value in case of failure.
(defn-over-http list-resources [])
```

Now you can call that function from anywhere in the frontend

```clojure
(:require [fullstack.api :as api])
(def list-resources (api/list-resources #(prn "result: " %)))
(list-resources "")
```

and the corresponding function `list-resources` on the backend is called. The argument
map gets transported over http via `transit-clj(s)`. The return value comes via callback instead of as a return value, as per usual in a non-blocking js env.

From the example it should be clear that this method pays off pretty quickly if you have multiple such functions. For each extra function one only would have to add one argument to `defdispatch` and then another `defn-over-http` line.

## Server arguments

This piece in [clj/fullstack/api.clj](./src/example/clj/fullstack//api.clj)

```clojure
(defn wrap-auth [handle]
  (fn [req]
    (handle (assoc-in req [:body :server-args :permissions] 
                      ;; try setting nil instead "all" 
                      "all")))) 
```

would take information from the request headers and convert them to server side permissions. 
In fact anything wrapped into `[:body :server-args]` will get passed into the first parameter list
of the api callable function (see [clj/fullstack/resources.clj](./src/example/clj/fullstack/resources.clj).

## Error handling

An error handler passed as an argument at the call site can be used 
instead of the `base-error-handler` provided at the declaration site.

```clojure
(def list-resources (api/list-resources 
                     #(prn %) 
                     #(prn "err:" %)))
(list-resources "")
```

Note that the regular handler is always called, for which we can
provide the default return value in case it should not be `nil`. In 
the motivating example `[]` has been chosen to match the return type
of the original function.

## Configuration

The arity 1 version of `defn-over-http` has `nil` as 
the default return value.

Here is an explanation of the available configuration options
at the declaration site:

```clojure
(ns fullstack.api
 (:require [fullstack.utils :refer [defn-over-http]]))

;; Set to nil if no 'global' error handling should be done.
;; Note we can always provide local error-handlers, 
;; as explained below.
(def base-error-handler #(prn "err:" %)) 

;; Useful for authentication.
(defn fetch-base-headers [] {})

;; In the arity 1 or 2 versions, defn-over http needs
;; the fetch-base-headers and base-error-handler functions
;; to having been defined.
(defn-over-http list-resources [])

;; In the arity 3 version, 
;; we can provide them on a case by case basis.
(defn-over-http list-resources 
                []
                {:base-error-handler base-error-handler
                 :fetch-base-headers fetch-base-headers})
```

## Example application

Execute once

    $ npm i

To start the API, run

    $ lein ring server-headless 

To start the UI, run

    $ lein fig:build   

This will open the app in a browser at `localhost:9500`.