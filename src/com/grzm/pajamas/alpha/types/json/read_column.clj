(ns com.grzm.pajamas.alpha.types.json.read-column
  (:require
   [com.grzm.pajamas.alpha.types :as types]
   [com.grzm.pajamas.alpha.types.util.json :as json])
  (:import
   (org.postgresql.util PGobject)))

(defmethod types/read-column :json
  [_ obj _ _]
  (json/pg-object-to-json obj))
