(ns com.grzm.pajamas.alpha.types.util.enum)

(defmulti type-value namespace)

(defmethod type-value :default
  [x]
  (throw (ex-info (format "Unknown enum type-name dispatch value \"%s\"" x)
                  {:dispatch-value x, :cause ::unknown-enum-dispatch})))
