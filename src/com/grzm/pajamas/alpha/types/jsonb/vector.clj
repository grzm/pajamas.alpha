(ns com.grzm.pajamas.alpha.types.jsonb.vector
  (:require
   [com.grzm.pajamas.alpha.types :as types]
   [com.grzm.pajamas.alpha.types.util.json :as json]))

(defmethod types/sql-parameter clojure.lang.IPersistentVector
  [value _ _]
  (json/create-pg-object :jsonb value))

(defmethod types/sql-parameter-type [:jsonb clojure.lang.IPersistentVector]
  [type-name-kw value _ _]
  (json/create-pg-object type-name-kw value))
