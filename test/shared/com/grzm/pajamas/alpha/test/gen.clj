(ns com.grzm.pajamas.alpha.test.gen
  (:require
   [cheshire.core :as json]
   [clojure.string :as str]
   [clojure.test.check.generators :as gen]))

(def string-alpha (gen/fmap str/join (gen/vector gen/char-alpha)))

(def date (gen/fmap #(java.util.Date. %) gen/int))
(def sql-date (gen/fmap #(java.sql.Date. %) gen/int))

(def json-like
  (let [scalars (gen/one-of [gen/int gen/boolean gen/string-alphanumeric])
        compound (fn [inner-gen]
                   (gen/one-of [(gen/vector inner-gen)
                                (gen/map gen/keyword inner-gen)]))]
    (gen/recursive-gen compound scalars)))

(def non-scalar-json-like
  (gen/one-of [(gen/vector json-like)
               (gen/map gen/keyword json-like)]))

(def json-like-object
  (gen/map gen/keyword json-like))

(def json-scalar-str
  (gen/fmap (fn [val] [val (json/generate-string val)])
            (gen/one-of [gen/int
                         gen/string-ascii
                         gen/boolean])))
