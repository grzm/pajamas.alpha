(ns com.grzm.pajamas.alpha.types.no-extensions-test
  (:require
   [clojure.test :refer :all]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as gen]
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as sgen]
   [com.grzm.pajamas.alpha.error.conditions :as cond]
   [com.grzm.pajamas.alpha.test.helpers :refer :all]
   [com.grzm.pajamas.alpha.test.gen :as pjgen]
   [com.grzm.pique.alpha.env.jdbc :as env]))

(defspec test-int-arrays
  (uninferred-vals (env/spec) (gen/vector gen/int)))

(defspec test-typed-int-arrays
  (error-vals (env/spec) "INT[]" (gen/vector gen/int) ::cond/invalid-parameter-type))

(defspec test-boolean-arrays
  (uninferred-vals (env/spec) (gen/vector gen/boolean)))

(defspec test-simple-maps (uninferred-vals (env/spec) (s/gen map?)))

(defspec test-json-like-vals (uninferred-vals (env/spec) pjgen/non-scalar-json-like))

(defspec test-json-like-maps (uninferred-vals (env/spec) pjgen/json-like-object))

(defspec test-keywords (uninferred-vals (env/spec) gen/keyword))
