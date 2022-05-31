# defn-over-http
 
A small library that allows users to call *Clojure* backend 
functions from *ClojureScript* frontend code. Arguments and return values
are passed back and forth via the [transit-format](https://github.com/cognitect/transit-format).

## Usage

To fetch the dependency via Leiningen use `[net.eighttrigrams/defn-over-http "0.1.14"]`.

## Rationale

Assuming an webapp setup where you have a frontend, say a single
page application, and a backend API, which is primarily made to support
ajax calls from the frontend, to supply it with data.

Now, let's say on the backend you have a function. Furthermore let's say you want to call this function from the frontend, with the least amount of extra code to organize the communication between frontend and backend.

What if all it would take would be some sort of declaration that such a function 
exists and should be wired up? It then could approximate something like a regular function call, only that it is across the wire.

## Walkthrough

Note that an [example application](#example-application) is part of this repository. 

Let `list-public-resources` be our function on the backend:

```clojure
(ns fullstack.resources)

(defn list-public-resources [query-string] ...) ; returns a vector
```

To declare it callable, all that is necessary is something like this:

```clojure
(ns fullstack.dispatch
 (:require 
  [net.eighttrigrams.defn-over-http.core :refer [defdispatch]]
  ;; Refer to the original function
  [fullstack.resources :refer [list-public-resources]])) 
    
;; Declare the function as api callable
(defdispatch handler list-public-resources)
```

To declare this same function on the frontend you'll have to set up something like
this:

```clojure
(ns fullstack.api
 (:require-macros 
  [net.eighttrigrams.defn-over-http.core 
   :refer [defn-over-http]])
 (:require ajax.core))

(def config {:api-path "/"
             :error-handler #(prn %)})

(defn-over-http list-public-resources {:return-value []})
```

This will "create" and make available on the frontend a function
with the corresponding (!) name. For the configuration maps see
sections [Configuration](#configuration) and [Error handling](#error-handling).

Now you can call that function from anywhere in the frontend

```clojure
(:require 
 [fullstack.api :as api]
 [cljs.core.async :refer [go]]
 [cljs.core.async.interop :refer-macros [<p!]])

(go (-> (api/list-public-resources "") <p! prn))
```

and the corresponding function `list-public-resources` on the backend is called. The argument map gets transported over http via `transit-clj(s)`. The return value comes as a promise, for we are in a non-blocking js environment.

From the example it should be clear that this method pays off pretty quickly if you have multiple such functions. For each extra function one only would have to add one argument to `defdispatch` and then another `defn-over-http` line.

## Configuration

In the namespace where `defn-over-http` is used, a config map must be present.
It can contain any of the keys `:api-path`, `:error-handler`, `:return-value`
and `:fetch-base-headers`. In its arity-2 version `defn-over-http` takes an
additional map, which gets merged into the config map.

Note that keys and values can also be passed varargs-style

```clojure
(defn-over-http list-public-resources :return-value [])
```

## Error handling

When an `:error-handler` is present, it must come in conjuction with a definition
for `:return-handler`. This is because in this case no error will be thrown at the call site. So the consumer of the succesful promise there should get an empty result matching the type of the values that our original function returns.

If however, no error handler is defined at the declaration site, errors must be caught at the call site.

```clojure
(go (try (-> (api/list-public-resources "") <p! prn))
         (catch js/Error err (prn (.-cause err))))
```

## Server arguments

In our example we used `(defdispatch handler list-public-resources)`. There exists a variant: 

```clojure
(defdispatch-with-args handler list-resources)
```

This one is used when with the following scenario in mind: Let's say we want to implement
authentication. Then on the frontend we would implement `(defn fetch-base-headers [] <..>)` meaningfully. On the backend side, we would want our function to receive corresponding permissions
in addition to the usual arguments coming from the frontend.

```clojure
(defn list-resources [{permissions :permissions}]
  (fn [& args] .. <do something with the permissions and the arguments>))
```

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
of `list-resources` (see [clj/fullstack/resources.clj](./src/example/clj/fullstack/resources.clj) in case `defdispatch-with-args` is used.

## Example application

Execute once

    $ npm i

To start the API, run

    $ lein ring server-headless 

To start the UI, run

    $ lein fig:build   

This will open the app in a browser at `localhost:9500`.