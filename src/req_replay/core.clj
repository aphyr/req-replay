(ns req-replay.core
  (:require [clj-yaml.core :as yaml]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.pprint :refer [pprint]]))

(def base "https://api.stripe.com")

(defn parse
  "Given a filename with requests and responses, yield a sequence of ops like
  {:request ..., :response ...}."
  [filename]
  (yaml/parse-string (slurp filename)))

(defn full-url
  "Expand a relative uri from a request into a full url."
  [url]
  (str base url))

(defn make-request
  "Given a request object, perform the request over HTTP and return a
  response."
  [req]
  (http/request {:method            (keyword (.toLowerCase (:method req)))
                 :url               (full-url (:url req))
                 :headers           (:headers req)
                 :body              (:body req)
                 :throw-exceptions  false}))

(defn check-op
  "Given an operation, make the http request given and compare to the response."
  [op]
  (= (:code (:response op))
     (:status (make-request (:request op)))))

(defn check
  "Given a filename, check every operation in it."
  [filename]
  (->> filename
       parse
       (pmap check-op)))
