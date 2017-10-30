(ns com.grzm.pajamas.alpha.error-test
  (:require
   [clojure.test :refer :all]
   [clojure.java.jdbc :as jdbc]
   [clojure.set :as set]
   [com.grzm.pajamas.alpha.error :as error]
   [com.grzm.pajamas.alpha.error.conditions :as cond]
   [com.grzm.pique.alpha.env.jdbc :as env])
  (:import (org.postgresql.util PSQLException)))

(deftest condition-test
  (testing "existing code as string"
    (is (= ::cond/successful-completion (error/condition "00000"))))
  (testing "existing code as keyword"
    (is (= ::cond/successful-completion (error/condition :00000))))
  (testing "with exception"
    (try
      (jdbc/query (env/spec) "SELECT 1/0")
      (throw (Exception. "expected exception"))
      (catch PSQLException e
        (is (= ::cond/division-by-zero (error/condition e))))))
  (testing "non-existant code"
    (is (nil? (error/condition "no-such-condition"))))
  (testing "postgresql and jdbc conditions don't intersect"
    (is (empty? (set/intersection (set (keys cond/postgresql-conditions))
                                  (set (keys cond/jdbc-conditions)))))))

(deftest server-error-message
  (try
    (jdbc/query (env/spec) "SELECT 1/0")
    (throw (Exception. "expected exception"))
    (catch PSQLException e
      (is (= {:SQLState "22012"
              :internalPosition 0
              :message "division by zero"
              :position 0
              :severity "ERROR"}
             (error/server-error-message e))))))
