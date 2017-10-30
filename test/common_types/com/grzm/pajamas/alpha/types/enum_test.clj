(ns com.grzm.pajamas.alpha.types.enum-test
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.test :refer :all]
   [com.grzm.pajamas.alpha.test.helpers :refer :all]
   [com.grzm.pajamas.alpha.types :as types]
   [com.grzm.pajamas.alpha.types.sql-parameter-types.keyword]
   [com.grzm.pique.alpha.env.jdbc :as env])
  (:import
   (org.postgresql.util PGobject)))

(defn round-trip-enum [spec type-name-kw val]
  (let [pg-type-name (name type-name-kw)
        pg-val (name val)]
    (testing (str "testing enum " pg-type-name))
    (jdbc/with-db-transaction [txn spec]
      (jdbc/db-do-commands txn (format "DROP TYPE IF EXISTS %s" pg-type-name))
      (jdbc/db-do-commands txn (format "CREATE TYPE %s AS ENUM ('%s')" pg-type-name pg-val))
      (val-query txn [(format "SELECT CAST(? AS %s)" pg-type-name) val]))))

(def enum-type-name :color)
(def enum-value :red)

(defmethod types/sql-parameter-type [enum-type-name clojure.lang.Keyword]
  [type-name-kw value _ _]
  (doto (PGobject.)
    (.setType (name type-name-kw))
    (.setValue (name value))))

(defmethod types/read-column enum-type-name
  [_ value _ _]
  (keyword value))

(deftest test-enum
  (is (= enum-value (round-trip-enum (env/spec) enum-type-name enum-value))))
