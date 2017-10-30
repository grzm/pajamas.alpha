(ns com.grzm.pajamas.alpha.types.jsonb.seq
  (:require
   [com.grzm.pajamas.alpha.types :as types]
   [com.grzm.pajamas.alpha.types.util.json :as json]))

(defmethod types/sql-parameter clojure.lang.ISeq
  [value _ _]
  (json/create-pg-object :jsonb value))

(defmethod types/sql-parameter-type [:jsonb clojure.lang.ISeq]
  [type-name-kw value _ _]
  (json/create-pg-object type-name-kw value))
