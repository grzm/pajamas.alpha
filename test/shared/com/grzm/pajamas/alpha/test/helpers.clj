(ns com.grzm.pajamas.alpha.test.helpers
  (:refer-clojure :exclude [type])
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.set :as set]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]
   [com.grzm.pajamas.alpha.error :as error]
   [com.grzm.pajamas.alpha.error.conditions :as conditions])
  (:import
   (org.postgresql.util PSQLException)
   (java.io BufferedReader StringReader)))

(defn val-query [db sql-params]
  (-> (jdbc/query db sql-params {:as-arrays? true}) second first))

(defn test-vals
  ([db sql gen]
   (test-vals db sql gen =))
  ([db sql gen eq-fn]
   (prop/for-all [val gen]
                 (eq-fn val (val-query db [sql val])))))

(defn inferred-vals
  ([db gen]
   (inferred-vals db gen =))
  ([db gen eq-fn]
   (test-vals db "SELECT ?" gen eq-fn)))

(defn typed-vals
  ([db pg-type-name gen]
   (typed-vals db pg-type-name gen =))
  ([db pg-type-name gen eq-fn]
   (test-vals db (format "SELECT CAST(? AS %s)" pg-type-name) gen eq-fn)))

(defn sql-state-for-condition [condition]
  (get (set/map-invert conditions/conditions) condition))

(defn error-vals*
  [db sql gen expected-condition]
  (let [expected-sql-state (sql-state-for-condition expected-condition)]
    (prop/for-all [val gen]
                  (try
                    (jdbc/query db [sql val])
                    (throw (ex-info "expected exception"
                                    {:val val
                                     :sql-state expected-sql-state
                                     :condition expected-condition}))
                    (catch PSQLException e
                      (let [sql-state (keyword (.getSQLState e))]
                        (if (= expected-sql-state sql-state)
                          true
                          (throw (ex-info "wrong exception"
                                          {:val val
                                           :expected {:sql-state expected-sql-state
                                                      :condition expected-condition}
                                           :actual {:sql-state sql-state
                                                    :condition (error/condition sql-state)}}
                                          e)))))))))

(defn error-vals
  ([db gen expected-condition]
   (error-vals* db "SELECT ?" gen expected-condition))
  ([db pg-type-name gen expected-condition]
   (error-vals* db (format "SELECT CAST(? AS %s)" pg-type-name) gen expected-condition)))

(defn uninferred-vals [db gen]
  (error-vals db gen ::conditions/invalid-parameter-type))

(defn bytes= [a b]
  (= (seq a) (seq b)))

(defn make-string-reader [s]
  (BufferedReader. (StringReader. s)))
