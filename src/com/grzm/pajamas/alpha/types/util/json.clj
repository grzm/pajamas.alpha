(ns com.grzm.pajamas.alpha.types.util.json
  (:require
   [cheshire.core :as json]
   [com.grzm.pajamas.alpha.types :as types])
  (:import
   (org.postgresql.util PGobject)))

(defn pg-object-to-json [obj] (json/parse-string (.getValue obj) true))

(defn create-pg-object
  [type-name-kw value]
  (doto (PGobject.)
    (.setType (name type-name-kw))
    (.setValue (json/generate-string value))))
