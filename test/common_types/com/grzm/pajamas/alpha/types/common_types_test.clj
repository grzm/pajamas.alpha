(ns com.grzm.pajamas.alpha.types.common-types-test
  (:require
   [clojure.spec.alpha :as s]
   [clojure.test :refer :all]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [com.grzm.pajamas.alpha.error.conditions :as cond]
   [com.grzm.pajamas.alpha.test.gen :as pjgen]
   [com.grzm.pajamas.alpha.test.helpers :refer :all]
   [com.grzm.pajamas.alpha.types.common-types]
   [com.grzm.pique.alpha.env.jdbc :as env]))

(defspec test-int-arrays
  (error-vals (env/spec) (gen/vector gen/int) ::cond/indeterminate-datatype))

(defspec test-typed-vec-int-arrays
  (typed-vals (env/spec) "INT[]" (gen/vector gen/int)))

(defspec test-typed-list-int-arrays
  (typed-vals (env/spec) "INT[]" (gen/list gen/int)))

(defspec test-boolean-arrays
  (error-vals (env/spec) (gen/vector gen/boolean) ::cond/indeterminate-datatype))

(defspec test-simple-maps
  (error-vals (env/spec) (s/gen map?) ::cond/indeterminate-datatype))

(defspec test-json-like-vals
  (error-vals (env/spec) pjgen/non-scalar-json-like ::cond/indeterminate-datatype))

(defspec test-json-like-object
  (error-vals (env/spec) pjgen/json-like-object ::cond/indeterminate-datatype))

(defspec test-json-like-object-typed-as-json
  (typed-vals (env/spec) "json" pjgen/json-like-object))

(defspec test-non-scalar-json-like-typed-as-json
  (typed-vals (env/spec) "json" pjgen/non-scalar-json-like))

(defspec test-json-like-object-typed-as-jsonb
  (typed-vals (env/spec) "jsonb" pjgen/json-like-object))

(defspec test-non-scalar-json-like-typed-as-jsonb
  (typed-vals (env/spec) "jsonb" pjgen/non-scalar-json-like))

(defn pair-props [pg-type-name gen]
  (prop/for-all [[val str-val] gen]
                (= val (val-query
                         (env/spec) [(format "SELECT CAST(? AS %s)" pg-type-name) str-val]))))

(defspec test-serialized-json-scalars (pair-props "json" pjgen/json-scalar-str))

(defspec test-serialized-jsonb-scalars (pair-props "jsonb" pjgen/json-scalar-str))

