(ns com.grzm.pajamas.alpha.types.hstore.map
  (:require
   [com.grzm.pajamas.alpha.types :as types]
   [com.grzm.pajamas.alpha.types.array :as array]))

(defmethod types/sql-parameter clojure.lang.IPersistentMap
  [value _ _]
  (types/create-pg-object :hstore value))

(defmethod types/sql-parameter-type [:hstore clojure.lang.IPersistentMap]
  [type-name-kw value _ _]
  (types/create-pg-object type-name-kw value))

(extend-protocol array/ElementTypeName
  clojure.lang.IPersistentMap
  (-element-type-name [obj] "hstore"))
