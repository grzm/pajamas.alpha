(ns com.grzm.pajamas.alpha.types.read-column-test
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.test :refer [deftest is]]
   [com.grzm.pajamas.alpha.query :as q]
   [com.grzm.pique.alpha.env.jdbc :as env])
  (:import
   (clojure.lang LazySeq PersistentArrayMap PersistentVector)
   (java.lang String)
   (org.postgresql.util PGobject)))

(deftest pg-enum-types
  (let [type-name "color"
        enum-val  "red"]
    (jdbc/with-db-transaction [txn (env/spec)]
      (jdbc/db-do-commands txn (format "DROP TYPE IF EXISTS %s" type-name))
      (jdbc/db-do-commands txn (format "CREATE TYPE %s AS ENUM ('%s')"
                                       type-name enum-val))
      (is (instance? String (q/val (env/spec)
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
    (is (instance? PGobject (:json_null_col res)))
    (is (instance? PGobject (:json_boolean_col res)))
    (is (instance? PGobject (:json_string_col res)))
    (is (instance? PGobject (:json_int_col res)))
    (is (instance? PGobject (:json_float_col res)))
    (is (instance? PGobject (:json_array_col res)))
    (is (instance? PGobject (:json_object_col res)))
    (is (instance? PGobject (:jsonb_null_col res)))
    (is (instance? PGobject (:jsonb_boolean_col res)))
    (is (instance? PGobject (:jsonb_string_col res)))
    (is (instance? PGobject (:jsonb_int_col res)))
    (is (instance? PGobject (:jsonb_float_col res)))
    (is (instance? PGobject (:jsonb_array_col res)))
    (is (instance? PGobject (:jsonb_object_col res)))))

(jdbc/when-available org.postgresql.jdbc.PgArray
  (deftest pg-array-types
    (let [res (q/row (env/spec)
                     (q/str "SELECT"
                            "ARRAY[1,2,3]::int[] AS int_array,"
                            "ARRAY[[1,2],[3,4]]::int[] AS int_2d_array,"
                            "ARRAY['one','two','three']::text[] AS text_array,"
                            "ARRAY[['one','two'],['three','four']]::text[] AS text_2d_array,"
                            "ARRAY[true,false]::boolean[] AS boolean_array,"
                            "ARRAY[[true,true],[false,false]]::int[] AS boolean_2d_array"))]
      (is (instance? org.postgresql.jdbc.PgArray (:int_array res)))
      (is (instance? org.postgresql.jdbc.PgArray (:int_2d_array res)))
      (is (instance? org.postgresql.jdbc.PgArray (:text_array res)))
      (is (instance? org.postgresql.jdbc.PgArray (:text_2d_array res)))
      (is (instance? org.postgresql.jdbc.PgArray (:boolean_array res)))
      (is (instance? org.postgresql.jdbc.PgArray (:boolean_2d_array res))))))

(jdbc/when-available org.postgresql.jdbc42.Jdbc42Array
  (deftest pg-array-types
    (let [res (q/row (env/spec)
                     (q/str "SELECT"
                            "ARRAY[1,2,3]::int[] AS int_array,"
                            "ARRAY[[1,2],[3,4]]::int[] AS int_2d_array,"
                            "ARRAY['one','two','three']::text[] AS text_array,"
                            "ARRAY[['one','two'],['three','four']]::text[] AS text_2d_array,"
                            "ARRAY[true,false]::boolean[] AS boolean_array,"
                            "ARRAY[[true,true],[false,false]]::int[] AS boolean_2d_array"))]
      (is (instance? org.postgresql.jdbc42.Jdbc42Array (:int_array res)))
      (is (instance? org.postgresql.jdbc42.Jdbc42Array (:int_2d_array res)))
      (is (instance? org.postgresql.jdbc42.Jdbc42Array (:text_array res)))
      (is (instance? org.postgresql.jdbc42.Jdbc42Array (:text_2d_array res)))
      (is (instance? org.postgresql.jdbc42.Jdbc42Array (:boolean_array res)))
      (is (instance? org.postgresql.jdbc42.Jdbc42Array (:boolean_2d_array res))))))

(jdbc/when-available org.postgresql.jdbc4.Jdbc4Array
  (deftest pg-array-types
    (let [res (q/row (env/spec)
                     (q/str "SELECT"
                            "ARRAY[1,2,3]::int[] AS int_array,"
                            "ARRAY[[1,2],[3,4]]::int[] AS int_2d_array,"
                            "ARRAY['one','two','three']::text[] AS text_array,"
                            "ARRAY[['one','two'],['three','four']]::text[] AS text_2d_array,"
                            "ARRAY[true,false]::boolean[] AS boolean_array,"
                            "ARRAY[[true,true],[false,false]]::int[] AS boolean_2d_array"))]
      (is (instance? org.postgresql.jdbc4.Jdbc4Array (:int_array res)))
      (is (instance? org.postgresql.jdbc4.Jdbc4Array (:int_2d_array res)))
      (is (instance? org.postgresql.jdbc4.Jdbc4Array (:text_array res)))
      (is (instance? org.postgresql.jdbc4.Jdbc4Array (:text_2d_array res)))
      (is (instance? org.postgresql.jdbc4.Jdbc4Array (:boolean_array res)))
      (is (instance? org.postgresql.jdbc4.Jdbc4Array (:boolean_2d_array res))))))
