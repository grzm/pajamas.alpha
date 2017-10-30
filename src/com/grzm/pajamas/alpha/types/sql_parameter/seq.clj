(ns com.grzm.pajamas.alpha.types.sql-parameter.seq
  (:require
   [clojure.java.jdbc :as jdbc]
   [com.grzm.pajamas.alpha.types :as types])
  (:import
   (java.sql PreparedStatement)))

(extend-protocol jdbc/ISQLParameter
  clojure.lang.ISeq
  (set-parameter [value ^PreparedStatement stmt ^long idx]
    (.setObject stmt idx (types/convert-sql-parameter value stmt idx))))
