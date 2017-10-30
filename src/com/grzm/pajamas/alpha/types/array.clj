(ns com.grzm.pajamas.alpha.types.array
  (:require
   [com.grzm.pajamas.alpha.types :as types]))

(defprotocol ElementTypeName
  (-element-type-name [obj]))

(defn element-type-name [obj]
  (-element-type-name obj))

(extend-protocol ElementTypeName
  java.lang.Object
  (-element-type-name [obj] "text")

  nil
  (-element-type-name [obj] "text")

  java.lang.Boolean
  (-element-type-name [obj] "boolean")

  java.lang.String
  (-element-type-name [obj] "text")

  java.lang.Long
  (-element-type-name [obj] "int4")

  java.lang.Integer
  (-element-type-name [obj] "int8")

  java.lang.Float
  (-element-type-name [obj] "float4")

  java.lang.Double
  (-element-type-name [obj] "float8")

  java.math.BigDecimal
  (-element-type-name [obj] "numeric")

  java.util.UUID
  (-element-type-name [obj] "uuid"))

(defn array-element-type-name
  [type-name]
  (cond
    (.startsWith type-name "_")
    (subs type-name 1)

    (.endsWith type-name "[]")
    (subs type-name 0 (- (count type-name) 2))

    :else
    (throw (ex-info "Unknown array element type name"
                    {:cause     ::unknown-array-element-type-name
                     :type-name type-name}))))

(defn- create-pg-array*
  [type-name value stmt]
  (.createArrayOf (.getConnection stmt) type-name (to-array value)))

(defn create-pg-array
  ([value stmt]
   (let [type-name (element-type-name (first value))]
     (create-pg-array* type-name value stmt)))
  ([type-name-kw value stmt idx]
   (let [type-name (-> (.getParameterMetaData stmt)
                       (.getParameterTypeName idx)
                       array-element-type-name)]
     (create-pg-array* type-name value stmt))))
