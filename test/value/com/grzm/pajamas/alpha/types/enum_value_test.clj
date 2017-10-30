(ns com.grzm.pajamas.alpha.types.enum-value-test
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.test :as test :refer [deftest is testing]]
   [com.grzm.pajamas.alpha.test.helpers :refer :all]
   [com.grzm.pajamas.alpha.types :as types]
   [com.grzm.pajamas.alpha.types.enum.keyword :as kw]
   [com.grzm.pajamas.alpha.types.sql-parameter.keyword]
   [com.grzm.pajamas.alpha.types.util.enum :as enum]
   [com.grzm.pique.alpha.env.jdbc :as env])
  (:import
   (org.postgresql.util PGobject)))

(defmethod enum/type-value "pajamas.color"
  [kw]
  [(namespace kw) (name kw)])

(defmethod types/read-column :color
  [_ value rs-meta idx]
  (keyword "pajamas.color" value))

(defn round-trip-enum
  [spec schema-name type-name enum-value kw-value]
  (testing (str "testing enum " type-name)
    (let [drop-schema (format "DROP SCHEMA IF EXISTS %s CASCADE" schema-name)
          create-schema (format "CREATE SCHEMA %s" schema-name)
          create-enum-type (format "CREATE TYPE %s AS ENUM ('%s')"
                                   type-name enum-value)]
      (jdbc/with-db-transaction [txn spec]
        (jdbc/db-do-commands txn drop-schema)
        (jdbc/db-do-commands txn create-schema)
        (jdbc/db-do-commands txn create-enum-type)
        (val-query txn [(format "SELECT CAST(? AS %s)" type-name) kw-value])))))

(deftest test-namespaced-keyword-enum
  (let [kw-value :pajamas.color/red]
    (is (= kw-value (round-trip-enum
                      (env/spec) "pajamas" "pajamas.color" "red" kw-value)))))
