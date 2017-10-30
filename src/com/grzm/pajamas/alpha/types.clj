(ns com.grzm.pajamas.alpha.types
  (:require
   [clojure.java.jdbc :as jdbc]
   [com.grzm.pajamas.alpha.types.util :as util])
  (:import
   (java.sql PreparedStatement)
   (org.postgresql.util PGobject)))

(defn dispatch-read-column
  [type-name-kw _ _ _]
  type-name-kw)

(defmulti read-column dispatch-read-column)

(defmethod read-column :default
  [type-name-kw value rs-meta idx]
  value)

(defn convert-column
  [value rs-meta idx]
  (let [type-name-kw (-> rs-meta
                         (.getColumnTypeName idx)
                         util/type-name-kw)]
    (read-column type-name-kw value rs-meta idx)))

(extend-protocol jdbc/IResultSetReadColumn
  Object
  (result-set-read-column [value rs-meta idx]
    (convert-column value rs-meta idx)))

;; dispatching only on Clojure type

(defn dispatch-sql-parameter
  "Dispatch on the types of both the value and the parameter."
  [value _ _]
  (type value))

(defmulti sql-parameter dispatch-sql-parameter)

(defmethod sql-parameter :default
  [value _ _]
  value)

(defn convert-sql-parameter
  [value ^PreparedStatement stmt ^long idx]
  (sql-parameter value stmt idx))

;; dispatching on Clojure type and PostgreSQL type

(defn dispatch-sql-parameter-type
  "Dispatch on the types of both the value and the parameter."
  [type-name-kw value _ _]
  [type-name-kw (type value)])

(defmulti sql-parameter-type dispatch-sql-parameter-type)

(defmethod sql-parameter-type :default
  [_ value _ _]
  value)

(defn convert-sql-parameter-type
  [value ^PreparedStatement stmt ^long idx]
  (let [type-name-kw (util/statement-type-name-kw stmt idx)]
    (sql-parameter-type type-name-kw value stmt idx)))

(defn create-pg-object
  [type-name-kw value]
  (doto (PGobject.)
    (.setType (name type-name-kw))
    (.setValue value)))

