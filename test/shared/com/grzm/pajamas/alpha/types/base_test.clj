(ns com.grzm.pajamas.alpha.types.base-test
  (:require
   [cheshire.core :as json]
   [clojure.java.jdbc :as jdbc]
   [clojure.test :refer :all]
   [clojure.test.check.clojure-test :refer [defspec]]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [com.grzm.pajamas.alpha.test.helpers :refer :all]
   [com.grzm.pajamas.alpha.error.conditions :as cond]
   [com.grzm.pajamas.alpha.query :as q]
   [com.grzm.pajamas.alpha.test.gen :as pjgen]
   [com.grzm.pique.alpha.env.jdbc :as env])
  (:import
   (clojure.lang Keyword LazySeq PersistentArrayMap PersistentVector)
   (java.lang Boolean Double Float Integer Long String)
   (java.math BigDecimal)
   (java.sql Date Time Timestamp)
   (java.util UUID)
   (org.postgresql.geometric PGbox PGcircle PGline PGlseg
                             PGpath PGpoint PGpolygon)
   (org.postgresql.util PSQLException
                        PGInterval
                        PGobject)))


;; got invalid byte sequence for generated string "Ô«" (with gen/string)
;; why?
;; (defspec test-strings (inferred-vals gen/string))

(defspec test-strings (inferred-vals (env/spec) gen/string-ascii))

(defspec test-ints (inferred-vals (env/spec) gen/int))
(defspec test-booleans (inferred-vals (env/spec) gen/boolean))
(defspec test-uuids (inferred-vals (env/spec) gen/uuid))
(defspec test-bytea (inferred-vals (env/spec) gen/bytes bytes=))

(defspec test-typed-ints (typed-vals (env/spec) "INT" gen/int))
(defspec test-typed-booleans (typed-vals (env/spec) "BOOLEAN" gen/boolean))
(defspec test-typed-texts (typed-vals (env/spec) "TEXT" gen/string-ascii))
(defspec test-typed-uuids (typed-vals (env/spec) "UUID" gen/uuid))
(defspec test-typed-bytea (typed-vals (env/spec) "BYTEA" gen/bytes bytes=))


;; can't cast scalars to json/b
(defspec test-ints-typed-as-jsonb
  (error-vals (env/spec) "jsonb" gen/int ::cond/cannot-coerce))

(defspec test-ints-typed-as-json
  (error-vals (env/spec) "json" gen/int ::cond/cannot-coerce))


;; note, we've overly restricted this to just alpha strings
(defspec test-strings-typed-as-jsonb
  (error-vals (env/spec) "jsonb" pjgen/string-alpha ::cond/invalid-text-representation))

(defspec test-strings-typed-as-json
  (error-vals (env/spec) "json" pjgen/string-alpha ::cond/invalid-text-representation))


(defspec test-boolean-typed-as-jsonb
  (error-vals (env/spec) "jsonb" gen/boolean ::cond/cannot-coerce))

(defspec test-boolean-typed-as-json
  (error-vals (env/spec) "json" gen/boolean ::cond/cannot-coerce))

(deftest pg-numeric-types
  (let [res (q/row (env/spec)
                   (q/str "SELECT"
                          "0::smallint AS smallint,"
                          "1::int AS int,"
                          "2::bigint AS bigint,"
                          "3.000::real AS real,"
                          "4.0000::double precision AS double,"
                          "5.000::numeric"))]
    (is (instance? Integer (:smallint res)))
    (is (instance? Integer (:int res)))
    (is (instance? Long (:bigint res)))
    (is (instance? Float (:real res)))
    (is (instance? Double (:double res)))
    (is (instance? BigDecimal (:numeric res)))))

(deftest pg-money-types
  (let [res (q/val (env/spec) (q/str "SELECT 2.3::money"))]
    (is (instance? Double res))))

(deftest pg-character-types
  (let [res (q/row (env/spec) (q/str "SELECT"
                                     "'some varchar'::varchar AS \"varchar\","
                                     "'some char'::char AS \"char\","
                                     "'some text'::text AS \"text\","
                                     "'\"char\"'::\"char\" AS internal_char,"
                                     "'internal_name'::name AS internal_name"
                                     ))]
    (is (instance? String (:varchar res)))
    (is (instance? String (:char res)))
    (is (instance? String (:text res)))
    (is (instance? String (:internal_char res)))
    (is (instance? String (:internal_name res)))))

(deftest pg-binary-data-types
  (let [res (q/val (env/spec) "SELECT E'\\\\xDEADBEEF'")]
    (is (instance? String res))))


(deftest pg-date-time-types
  (let [res (q/row (env/spec)
                   (q/str "SELECT"
                          "current_timestamp::timestamp AS timestamp_col,"
                          "current_timestamp::timestamptz AS timestamptz_col,"
                          "current_date::date AS date_col,"
                          "current_time::time AS time_col,"
                          "current_time::timetz AS timetz_col,"
                          "'1 hour'::interval AS interval_col"))]
    (is (instance? Timestamp (:timestamp_col res)))
    (is (instance? Timestamp (:timestamptz_col res)))
    (is (instance? Date (:date_col res)))
    (is (instance? Time (:time_col res)))
    (is (instance? Time (:timetz_col res)))
    (is (instance? PGInterval (:interval_col res)))))

(deftest pg-boolean-type
  (let [res (q/val (env/spec) "SELECT true::boolean AS boolean_col")]
    (is (instance? Boolean res))))

(deftest pg-geometric-types
  (let [res (q/row (env/spec)
                   (q/str "SELECT"
                          "'(1, 2)'::point AS point_col,"
                          ;; cast to line doesn't work on 9.4-1201-jdbc41
                          ;; fixed by 9.4-1206-jdbc41
                          "'{1, 2, 3}'::line AS line_col,"
                          "'((1,2), (3,4))'::lseg AS segment_col,"
                          "'((1,2), (3,4))'::box AS box_col,"
                          "'((1,2), (3,4), (5,6))'::path AS path_col,"
                          "'((1,2), (3,4), (5,6))'::polygon AS polygon_col,"
                          "'((1,2),3)'::circle AS circle_col"))]
    (is (instance? PGpoint (:point_col res)))
;;    (is (instance? PGline (:line_col res)))
    (is (instance? PGlseg (:segment_col res)))
    (is (instance? PGbox (:box_col res)))
    (is (instance? PGpath (:path_col res)))
    (is (instance? PGpolygon (:polygon_col res)))
    (is (instance? PGcircle (:circle_col res)))))

(deftest pg-network-address-types
  (let [res (q/row (env/spec)
                   (q/str "SELECT"
                          "'192.168.100.128/25'::cidr AS cidr_col,"
                          "'127.0.0.1'::inet AS ipv4_inet_col,"
                          "'2001:4f8:3:ba:2e0:81ff:fe22:d1f1'::inet AS ipv6_inet_col,"
                          "'08:00:2b:01:02:03'::macaddr AS macaddr_col"))]
    (is (instance? PGobject (:cidr_col res)))
    (is (instance? PGobject (:ipv4_inet_col res)))
    (is (instance? PGobject (:ipv6_inet_col res)))))

(deftest pg-bit-string-types
  (let [res (q/row (env/spec)
                   (q/str "SELECT"
                          "CAST(B'101' AS bit) AS bit_col,"
                          "CAST(B'00' AS bit varying) AS varbit_col"))]
    (is (instance? Boolean (:bit_col res)) "this is unexpected!")
    (is (instance? PGobject (:varbit_col res)))))

(deftest pg-text-search-types
  (let [res (q/row (env/spec)
                   (q/str "SELECT"
                          "'a fat cat sat on a mat'::tsvector AS tsvector_col,"
                          "'fat & rat'::tsquery AS tsquery_col"))]
    (is (instance? PGobject (:tsvector_col res)))
    (is (instance? PGobject (:tsquery_col res)))))

(deftest pg-uuid-type
  (let [res (q/val (env/spec)
                   (q/str "SELECT"
                          "'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::uuid AS uuid_col"))]
    (is (instance? UUID res))))

(jdbc/when-available org.postgresql.jdbc.PgSQLXML
  (deftest pg-xml-type
    (let [res (q/val (env/spec) "SELECT '<foo>bar</foo>'::xml AS uuid_col")]
      (is (instance? org.postgresql.jdbc.PgSQLXML res)))))

(deftest pg-composite-types
  (let [type-name "complex"
        enum-val "red"]
    (jdbc/with-db-transaction [txn (env/spec)]
      (jdbc/db-do-commands txn (format "DROP TYPE IF EXISTS %s" type-name))
      (jdbc/db-do-commands
        txn (format "CREATE TYPE %s AS (r double precision, i double precision)"
                                       type-name))
      (is (instance? PGobject
                     (q/val (env/spec) (format "SELECT ROW('%s', 1.0, 3.4)" type-name)))))))

(deftest pg-range-types
  (let [res (q/row (env/spec)
                   (q/str "SELECT"
                          "int4range(1, 2),"
                          "int8range(3, 4),"
                          "numrange(3.5, 6.8),"
                          "tsrange(current_timestamp::timestamp,"
                          "current_timestamp::timestamp + interval '1 hour'),"
                          "tstzrange(current_timestamp,"
                          "current_timestamp + interval '1 hour'),"
                          "daterange('2010-01-01', '2011-01-01')"))]
    (is (instance? PGobject (:int4range res)))
    (is (instance? PGobject (:int8range res)))
    (is (instance? PGobject (:numrange res)))
    (is (instance? PGobject (:tsrange res)))
    (is (instance? PGobject (:tstzrange res)))
    (is (instance? PGobject (:daterange res)))))
