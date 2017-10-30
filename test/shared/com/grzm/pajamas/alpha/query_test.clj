(ns com.grzm.pajamas.alpha.query-test
  (:require
   [clojure.test :refer [deftest is]]
   [com.grzm.pajamas.alpha.query :as q]
   [com.grzm.pique.alpha.env.jdbc :as env]
   [clojure.string :as str]))

(deftest query-str-joins-with-spaces
  (is (= "x y z" (q/str "x" "y" "z"))))

(defn inc-vals [r]
  (-> r first inc vector))

(defn inc-row-vals [r]
  (into {} (map (fn [[k v]] [k (inc v)]) r)))

(deftest val-returns-a-value
  (let [sql "SELECT 42"]
    (is (= 42 (q/val (env/spec) sql )))
    (is (= 43 (q/val (env/spec) sql {:row-fn inc-vals})))))

(deftest ary-returns-a-sequence
  (let [sql "VALUES (1), (2), (3)"]
    (let [res (q/ary (env/spec) sql)]
      (is (seq? res))
      (is (not (vector? res)))
      (is (= [1 2 3] res)))
    (let [res (q/ary (env/spec) sql {:row-fn inc-vals})]
      (is (= [2 3 4] res)))))

(deftest aryv-returns-a-vector
  (let [sql "VALUES (1), (2), (3)"]
    (let [res (q/aryv (env/spec) sql)]
      (is (vector? res))
      (is (= [1 2 3] res)))
    (let [res (q/aryv (env/spec) sql {:row-fn inc-vals})]
      (is (= [2 3 4] res)))))

(deftest row-returns-a-map
  (let [sql "SELECT 1 AS one, 2 AS two, 3 AS three"]
    (let [res (q/row (env/spec) sql)]
      (is (= {:one 1, :two 2, :three 3} res)))
    (let [res (q/row (env/spec) sql {:row-fn inc-row-vals})]
      (is (= {:one 2, :two 3, :three 4} res)))))

(deftest set-returns-a-sequence-of-maps
  (let [sql (q/str "SELECT one, two, three"
                   "FROM (VALUES"
                   "(7, 8, 9),"
                   "(4, 5, 6),"
                   "(1, 2, 3)) AS _ (one, two, three)")]
    (let [res (q/set (env/spec) sql)]
      (is (seq? res))
      (is (= [{:one 7, :two 8, :three 9}
              {:one 4, :two 5, :three 6}
              {:one 1, :two 2, :three 3}]
             res)))
    (let [res (q/set (env/spec) sql {:row-fn inc-row-vals})]
      (is (= [{:one 8, :two 9, :three 10}
              {:one 5, :two 6, :three 7}
              {:one 2, :two 3, :three 4}]
             res)))))

(deftest setv-returns-a-vector-of-maps
  (let [res (q/setv (env/spec)
                    (q/str "SELECT one, two, three"
                           "FROM (VALUES"
                           "(7, 8, 9),"
                           "(4, 5, 6),"
                           "(1, 2, 3)) AS _ (one, two, three)"))]
    (is (vector? res))
    (is (= [{:one 7, :two 8, :three 9}
            {:one 4, :two 5, :three 6}
            {:one 1, :two 2, :three 3}]
           res))))

(deftest additional-options-are-respected
  (let [res (q/row (env/spec) (q/str "SELECT 1 AS one, 2 AS two")
                   {:identifiers str/upper-case})]
    (is (= {:ONE 1, :TWO 2} res))))
