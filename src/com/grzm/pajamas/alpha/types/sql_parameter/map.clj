(ns com.grzm.pajamas.alpha.types.sql-parameter.map
  (:require
   [clojure.java.jdbc :as jdbc]
   [com.grzm.pajamas.alpha.types :as types])
  (:import
   (java.sql PreparedStatement)))

(extend-protocol jdbc/ISQLParameter
  clojure.lang.IPersistentMap
  (set-parameter [value ^PreparedStatement stmt ^long idx]
    (.setObject stmt idx (types/convert-sql-parameter value stmt idx))))
