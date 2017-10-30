(ns com.grzm.pajamas.alpha.types.jsonb.map
  (:require
   [com.grzm.pajamas.alpha.types :as types]
   [com.grzm.pajamas.alpha.types.array :as array]
   [com.grzm.pajamas.alpha.types.util.json :as json]))

(defmethod types/sql-parameter clojure.lang.IPersistentMap
  [value _ _]
  (json/create-pg-object :jsonb value))

(defmethod types/sql-parameter-type [:jsonb clojure.lang.IPersistentMap]
  [type-name-kw value _ _]
  (json/create-pg-object type-name-kw value))

(extend-protocol array/ElementTypeName
  clojure.lang.IPersistentMap
  (-element-type-name [obj] "jsonb"))
