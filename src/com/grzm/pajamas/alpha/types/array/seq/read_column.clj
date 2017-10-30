(ns com.grzm.pajamas.alpha.types.array.seq.read-column
  (:require
   [com.grzm.pajamas.alpha.types :as types]
   [com.grzm.pajamas.alpha.types.array :as array]))

(defmethod types/read-column :_
  [_ ary rs-meta idx]
  (let [type-name-kw (-> (.getColumnTypeName rs-meta idx)
                         array/element-type-name
                         keyword)]
    (map #(types/read-column type-name-kw % nil nil) (.getArray ary))))
