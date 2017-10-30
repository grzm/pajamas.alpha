(ns com.grzm.pajamas.alpha.types.read-column-test
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.test :refer [deftest is]]
   [com.grzm.pajamas.alpha.query :as q]
   [com.grzm.pique.alpha.env.jdbc :as env])
  (:import
   (clojure.lang Keyword LazySeq PersistentArrayMap PersistentVector)))

(comment
  (deftest pg-enum-types
    (let [type-name "color"
          enum-val  "red"]
      (jdbc/with-db-transaction [txn (env/spec)]
        (jdbc/db-do-commands txn (format "DROP TYPE IF EXISTS %s" type-name))
        (jdbc/db-do-commands txn (format "CREATE TYPE %s AS ENUM ('%s')"
                                         type-name enum-val))
        (is (instance? Keyword (q/val (env/spec)
                                      (format "SELECT CAST('%s' AS %s)"
                                              enum-val type-name)))))))

  (deftest pg-json-types
    (let [res (q/row (env/spec)
                     (q/str "SELECT"
                            "'null'::json AS json_null_col,"
                            "'true'::json AS json_boolean_col,"
                            "'\"a string\"'::json AS json_string_col,"
                            "'5'::json AS json_int_col,"
                            "'6.3'::json AS json_float_col,"
                            "'[1, 2, \"foo\", null]'::json AS json_array_col,"
                            "'{\"foo\": [true, \"bar\"]}'::json AS json_object_col,"
                            "'null'::jsonb AS jsonb_null_col,"
                            "'true'::jsonb AS jsonb_boolean_col,"
                            "'\"a string\"'::jsonb AS jsonb_string_col,"
                            "'5'::jsonb AS jsonb_int_col,"
                            "'6.3'::jsonb AS jsonb_float_col,"
                            "'[1, 2, \"foo\", null]'::jsonb AS jsonb_array_col,"
                            "'{\"foo\": [true, \"bar\"]}'::jsonb AS jsonb_object_col"))]
      (is (nil? (:json_null_col res)))
      (is (instance? Boolean (:json_boolean_col res)))
      (is (instance? String (:json_string_col res)))
      (is (instance? Integer (:json_int_col res)))
      (is (instance? Double (:json_float_col res)))
      (is (instance? LazySeq (:json_array_col res)))
      (is (instance? PersistentArrayMap (:json_object_col res)))
      (is (nil? (:jsonb_null_col res)))
      (is (instance? Boolean (:jsonb_boolean_col res)))
      (is (instance? String (:jsonb_string_col res)))
      (is (instance? Integer (:jsonb_int_col res)))
      (is (instance? Double (:jsonb_float_col res)))
      (is (instance? LazySeq (:jsonb_array_col res)))
      (is (instance? PersistentArrayMap (:jsonb_object_col res)))))

  (deftest pg-array-types
    (let [res (q/row (env/spec)
                     (q/str "SELECT"
                            "ARRAY[1,2,3]::int[] AS int_array,"
                            "ARRAY[[1,2],[3,4]]::int[] AS int_2d_array,"
                            "ARRAY['one','two','three']::text[] AS text_array,"
                            "ARRAY[['one','two'],['three','four']]::text[] AS text_2d_array,"
                            "ARRAY[true,false]::boolean[] AS boolean_array,"
                            "ARRAY[[true,true],[false,false]]::int[] AS boolean_2d_array"))]
      (is (instance? PersistentVector (:int_array res)))
      (is (instance? PersistentVector (:int_2d_array res)))
      (is (instance? PersistentVector (:text_array res)))
      (is (instance? PersistentVector (:text_2d_array res)))
      (is (instance? PersistentVector (:boolean_array res)))
      (is (instance? PersistentVector (:boolean_2d_array res))))))
