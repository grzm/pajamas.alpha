(ns com.grzm.pajamas.alpha.types.array.seq
  (:require
   [com.grzm.pajamas.alpha.types :as types]
   [com.grzm.pajamas.alpha.types.array :as array]))

(defmethod types/sql-parameter clojure.lang.ISeq
  [value stmt idx]
  (array/create-pg-array value stmt))

(defmethod types/sql-parameter-type [:_ clojure.lang.ISeq]
  [type-name-kw value stmt idx]
  (array/create-pg-array type-name-kw value stmt idx))
