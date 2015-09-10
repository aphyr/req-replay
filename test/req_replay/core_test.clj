(ns req-replay.core-test
  (:require [clojure.test :refer :all]
            [req-replay.core :refer :all]
            [clojure.pprint :refer [pprint]]))

(deftest check-test
  (->> "data/requestlog-simple.yaml"
       check
       pprint))
