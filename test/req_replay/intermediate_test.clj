(ns req-replay.intermediate-test
  (:require [clojure.test :refer :all]
            [req-replay.intermediate :refer :all]
            [clojure.pprint :refer [pprint]]))

(deftest transform-test
  (testing "number"
    (is (= 2 (transform 2 {2 3}))))

  (testing "string"
    (is (= "fxxxx" (transform "foo" {"o" "xx"})))))

(deftest transform*-test
  (is (= {:foo "fxxxx"}
         (transform* {"o" "xx"}
                     {:foo "foo"}))))

(deftest check-test
  (->> "data/requestlog-intermediate.yaml"
       check
       (every? true?)
       is))
