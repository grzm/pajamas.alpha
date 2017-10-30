(ns com.grzm.pajamas.alpha.types.util)

(defn type-name-kw [type-name]
  (if (or (= "_" (-> type-name name (subs 0 1)))
          (-> type-name name (.endsWith "[]")))
    :_
    (keyword type-name)))

(defn statement-type-name-kw
  [stmt idx]
  (-> (.getParameterMetaData stmt)
      (.getParameterTypeName idx)
      type-name-kw))
