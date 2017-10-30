(ns com.grzm.pajamas.alpha.query
  (:refer-clojure :exclude [set val str])
  (:require
   [clojure.java.jdbc :as jdbc]
   [clojure.string :as str]
   [com.grzm.pajamas.alpha.error :as error])
  (:import
   (java.sql PreparedStatement)
   (org.postgresql.util PSQLException)))

(defn str [& strs] (str/join " " strs))

(defn- sql-stmt?
  [expr]
  (or (string? expr)
      (instance? PreparedStatement expr)))

(defn- wrap-jdbc-call
  ([query-or-execute db sql-params]
   (wrap-jdbc-call query-or-execute db sql-params {}))
  ([query-or-execute db sql-params opts]
   (try
     (query-or-execute db sql-params opts)
     (catch PSQLException e
       (let [sql-params-vector (if (sql-stmt? sql-params)
                                 (vector sql-params)
                                 (vec sql-params))
             sql-state         (.getSQLState e)]
         (throw (ex-info
                  "got an exception in jdbc call"
                  (merge {:message   (.getMessage e)
                          :sql-state sql-state
                          :sql       (first sql-params-vector)
                          :params    (rest sql-params-vector)}
                         (when-let [condition (error/condition sql-state)]
                           {::error/condition condition})
                         (when-let [server-error-message (error/server-error-message e)]
                           {::error/server-error-message server-error-message}))
                  e)))))))

(defn query
  ([db sql-params] (query db sql-params {}))
  ([db sql-params opts] (wrap-jdbc-call jdbc/query db sql-params opts)))

(defn execute!
  ([db sql-params] (execute! db sql-params {}))
  ([db sql-params opts] (wrap-jdbc-call jdbc/execute! db sql-params opts)))

(defn val
  "Returns a single value. Equivalent to passing options
  `{:as-arrays? true, :result-set-fn (comp first second)}'"
  ([db sql-params] (val db sql-params {}))
  ([db sql-params opts]
   (query db
          sql-params
          (merge {:as-arrays? true, :result-set-fn (comp first second)}
                 opts))))

(defn ary
  "Returns a sequence of values. Equivalent to passing options
  `{:as-arrays? true, :result-set-fn #(->> % rest (map first))}`"
  ([db sql-params] (ary db sql-params {}))
  ([db sql-params opts]
   (query db
          sql-params
          (merge {:as-arrays? true, :result-set-fn #(->> % vec rest (map first))}
                 opts))))

(defn aryv
  "Returns a vector of values. Equivalent to passing options
  `{:as-arrays? true, :result-set-fn #(->> % vec rest (mapv first))}`"
  ([db sql-params] (aryv db sql-params {}))
  ([db sql-params opts]
   (query db
          sql-params
          (merge {:as-arrays? true, :result-set-fn #(->> % vec rest (mapv first))}
                 opts))))

(defn row
  "Returns a single row result set. Equivalent to passing options
  `{:as-arrays? true, :result-set-fn first}`"
  ([db sql-params] (row db sql-params {}))
  ([db sql-params opts]
   (query db sql-params (merge {:result-set-fn first} opts))))

(defn set
  "Returns the result set. Equivalent to passing no options."
  ([db sql-params] (set db sql-params {}))
  ([db sql-params opts] (query db sql-params opts)))

(defn setv
  "Returns the result set as a vector. Equivalent to passing options
  `{:result-set-fn vec}`"
  ([db sql-params] (setv db sql-params {}))
  ([db sql-params opts]
   (query db sql-params (merge {:result-set-fn vec} opts))))
