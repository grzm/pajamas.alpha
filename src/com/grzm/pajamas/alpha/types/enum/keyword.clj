(ns com.grzm.pajamas.alpha.types.enum.keyword
  (:require
   [com.grzm.pajamas.alpha.types :as types]
   [com.grzm.pajamas.alpha.types.util.enum :as enum])
  (:import
   (org.postgresql.util PGobject)))

(defmethod types/sql-parameter clojure.lang.Keyword
  [value stmt idx]
  (let [[type-name enum-value :as type-value] (enum/type-value value)]
    (doto (PGobject.)
      (.setType type-name)
      (.setValue enum-value))))
