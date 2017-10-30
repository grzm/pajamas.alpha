(ns com.grzm.pajamas.alpha.types.value-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :as test :refer [deftest is]]
   [clojure.test.check.clojure-test :as ct :refer [defspec]]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [com.grzm.pajamas.alpha.error.conditions :as cond]
   [com.grzm.pajamas.alpha.test.gen :as pjgen]
   [com.grzm.pajamas.alpha.test.helpers :refer :all]
   [com.grzm.pajamas.alpha.types.array.seq]
   [com.grzm.pajamas.alpha.types.array.vector.read-column]
   [com.grzm.pajamas.alpha.types.array.vector]
   [com.grzm.pajamas.alpha.types.json.read-column]
   [com.grzm.pajamas.alpha.types.jsonb.map]
   [com.grzm.pajamas.alpha.types.jsonb.read-column]
   [com.grzm.pajamas.alpha.types.sql-parameter.map]
   [com.grzm.pajamas.alpha.types.sql-parameter.seq]
   [com.grzm.pajamas.alpha.types.sql-parameter.vector]
   [com.grzm.pique.alpha.env.jdbc :as env]))

(defspec test-int-arrays
  (inferred-vals (env/spec) (gen/vector gen/int)))

(defspec test-typed-vec-int-arrays
  (typed-vals (env/spec) "INT[]" (gen/vector gen/int)))

(defspec test-typed-list-int-arrays
  (typed-vals (env/spec) "INT[]" (gen/list gen/int)))

(defspec test-boolean-arrays
  (inferred-vals (env/spec) (gen/vector gen/boolean)))

(defspec test-json-like-object
  (inferred-vals (env/spec) pjgen/json-like-object))

(defspec test-json-like-object-typed-as-json
  (typed-vals (env/spec) "json" pjgen/json-like-object))

(defspec test-json-like-object-typed-as-jsonb
  (typed-vals (env/spec) "jsonb" pjgen/json-like-object))

(defn pair-props [pg-type-name gen]
  (prop/for-all [[val str-val] gen]
                (let [sql-params [(format "SELECT CAST(? AS %s)" pg-type-name) str-val]]
                  (= val (val-query (env/spec) sql-params)))))

(defspec test-serialized-json-scalars (pair-props "json" pjgen/json-scalar-str))

(defspec test-serialized-jsonb-scalars (pair-props "jsonb" pjgen/json-scalar-str))
