(ns com.grzm.pajamas.alpha.types.sql-parameter-types.object
  (:require
   [clojure.java.jdbc :as jdbc]
   [com.grzm.pajamas.alpha.types :as types])
  (:import
   (java.sql PreparedStatement)))

(extend-protocol jdbc/ISQLParameter
  java.lang.Object
  (set-parameter [value ^PreparedStatement stmt ^long idx]
    (.setObject stmt idx (types/convert-sql-parameter-type value stmt idx))))
