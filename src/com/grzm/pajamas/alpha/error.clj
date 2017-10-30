(ns com.grzm.pajamas.alpha.error
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [com.grzm.pajamas.alpha.error.conditions :as conditions])
  (:import
   (org.postgresql.util PSQLException)))

(defn remove-nil [m] (into {} (remove (comp nil? second) m)))

(defn server-error-message
  "Parses the server error message for the given PSQLException"
  [^PSQLException e]
  (when-let [server-message (.getServerErrorMessage e)]
    (-> (bean server-message)
        (dissoc :file :routine :line :class)
        remove-nil)))

(defn condition
  "Returns the error condition for the given code"
  [code-or-ex]
  (let [code (if (instance? PSQLException code-or-ex)
               (.getSQLState code-or-ex)
               code-or-ex)]
    (get conditions/conditions (keyword code))))
