(ns req-replay.intermediate
  "A context is a mapping of expected identifiers to actual identifiers, used
  to remap requests from the file given changing identifiers from the actual
  HTTP responses."
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.pprint :refer [pprint]]
            [req-replay.core :as core]
            [clojure.walk :as walk]
            [clojure.string :as str]
            [cheshire.core :as json]))

(defprotocol Transform
  (transform [this context]))

(extend-protocol Transform
  nil
  (transform [x _] x)

  Object
  (transform [x _] x)

  String
  (transform [s context]
    (reduce (fn [s [expected actual]]
              (str/replace s expected actual))
            s
            context)))

(defn transform*
  "Given a context and a request, returns the request with all expected
  identifiers replaced by their actual identifers."
  [context req]
  (walk/prewalk #(transform % context) req))

(defn extract-context
  "Given an expected response and an actual response, construct a new context
  for future ops"
  [expected actual]
  (let [extractor (fn [x] (-> x :body json/parse-string (get "id")))]
    {(extractor expected) (extractor actual)}))

(defn check-op
  "Given [context oks] and an operation, make the http request given,
  transformed using the current context, and compare to the response, then
  return a pair of [context' [... op-ok?]]."
  [[context oks] op]
  (let [actual-res (->> op
                        :request
                        (transform* context)
                        core/make-request)
        expected-res (:response op)
        ok? (= (:code expected-res)
               (:status actual-res))
        context' (merge context (extract-context expected-res actual-res))]
    [context' (conj oks ok?)]))

(defn check
  "Given a filename, check every operation in it."
  [filename]
  (->> filename
       core/parse
       (reduce check-op
               [{} []])
       second))
