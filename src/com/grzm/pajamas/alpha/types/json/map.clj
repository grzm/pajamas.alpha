(ns com.grzm.pajamas.alpha.types.json.map
  (:require
   [com.grzm.pajamas.alpha.types :as types]
   [com.grzm.pajamas.alpha.types.array :as array]
   [com.grzm.pajamas.alpha.types.util.json :as json]))

(defmethod types/sql-parameter clojure.lang.IPersistentMap
  [value _ _]
  (json/create-pg-object :json value))

(defmethod types/sql-parameter-type [:json clojure.lang.IPersistentMap]
  [type-name-kw value _ _]
  (json/create-pg-object type-name-kw value))

(extend-protocol array/ElementTypeName
  clojure.lang.IPersistentMap
  (-element-type-name [obj] "json"))
