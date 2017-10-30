(def project 'com.grzm/pajamas.alpha)
(def version "0.1.0")

(set-env! :resource-paths #{"resources" "src" "test/resources"}
          :source-paths   #{"test/shared" "test/common_types"}
          :dependencies   '[[adzerk/boot-test "RELEASE" :scope "test"]
                            [boot-codox "0.10.3" :scope "test"]
                            [boot-fmt "0.1.6" :scope "test"]
                            [cheshire "5.7.1"]
                            [cider/cider-nrepl "0.15.0-SNAPSHOT" :scope "test"]
                            [enlive "1.1.6" :scope "test"]
                            [environ "1.1.0"]
                            [com.grzm/pique.alpha "0.1.0-SNAPSHOT" :scope "test"]
                            [com.grzm/tespresso.alpha "0.1.0-SNAPSHOT" :scope "test"]
                            [metosin/boot-alt-test "0.3.2" :scope "test"]
                            [org.clojure/clojure "RELEASE"]
                            [org.clojure/java.jdbc "0.7.0-beta2"]
                            [org.clojure/spec.alpha "0.1.123" :scope "test"]
                            [org.clojure/test.check "0.9.1-SNAPSHOT" :scope "test"]
                            [org.clojure/tools.logging "0.4.0"]
                            [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                            ;; comment out the PostgreSQL driver when testing other versions
                            [org.postgresql/postgresql "42.0.0"]
                            [refactor-nrepl "2.4.0-SNAPSHOT" :scope "test"]
                            [tolitius/boot-check "0.1.4-SNAPSHOT" :scope "test"]
                            [zprint "0.4.2" :scope "test"]])

(require '[cider.tasks :refer [add-middleware]])

(task-options!
  add-middleware {:middleware '[cider.nrepl.middleware.apropos/wrap-apropos
                                cider.nrepl.middleware.version/wrap-version
                                refactor-nrepl.middleware/wrap-refactor
                                cider.nrepl.middleware.refresh/wrap-refresh]})

(task-options!
  pom {:project     project
       :version     version
       :description "Comfy Clojure for Postgres and JDBC"
       :url         "http://github.com/grzm/pajamas"
       :scm         {:url "https://github.com/grzm/pajamas"}
       :license     {"MIT"
                     "https://opensource.org/licenses/MIT"}})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))


;;; testing

(require '[adzerk.boot-test :refer [test]])

(task-options!
  test {:exclusions '#{grzm.pajamas.test
                       grzm.pajamas.test.filter
                       grzm.pajamas.test.gen
                       grzm.pajamas.test.helpers
                       grzm.pajamas.env
                       grzm.pajamas.env.environment
                       grzm.pajamas.env.password
                       grzm.pajamas.env.service
                       grzm.pajamas.env.system-environment
                       grzm.pajamas.error
                       grzm.pajamas.error.conditions
                       grzm.pajamas.query
                       grzm.pajamas.types
                       grzm.pajamas.types.array
                       grzm.pajamas.types.array.seq
                       grzm.pajamas.types.array.seq.read-column
                       grzm.pajamas.types.array.vector
                       grzm.pajamas.types.array.vector.read-column
                       grzm.pajamas.types.common-types
                       grzm.pajamas.types.json.map
                       grzm.pajamas.types.json.read-column
                       grzm.pajamas.types.json.seq
                       grzm.pajamas.types.json.vector
                       grzm.pajamas.types.jsonb.map
                       grzm.pajamas.types.jsonb.read-column
                       grzm.pajamas.types.jsonb.seq
                       grzm.pajamas.types.jsonb.vector
                       grzm.pajamas.types.sql-parameter.keyword
                       grzm.pajamas.types.sql-parameter.map
                       grzm.pajamas.types.sql-parameter.seq
                       grzm.pajamas.types.sql-parameter.vector
                       grzm.pajamas.types.sql-parameter-types.keyword
                       grzm.pajamas.types.sql-parameter-types.map
                       grzm.pajamas.types.sql-parameter-types.object
                       grzm.pajamas.types.sql-parameter-types.seq
                       grzm.pajamas.types.sql-parameter-types.vector
                       grzm.pajamas.types.util
                       grzm.pajamas.types.util.json
                       grzm.tespresso
                       grzm.tespresso.clojure-tools-logging
                       grzm.tespresso.spec-test
                       }})

(require '[metosin.boot-alt-test :refer [alt-test]])

(deftask no-ext []
  (set-env! :source-paths #(-> (conj % "test/no_ext")
                               (disj "test/common_types")))
  identity)

(deftask with-values []
  (set-env! :source-paths #(-> (conj % "test/value")
                               (disj "test/common_types")))
  identity)

(defn remove-deps
  [deps]
  (->> (get-env :dependencies)
       (remove #(some (set deps) ((juxt first (comp symbol name first)) %)))
       vec))

(def default-jdbc-driver-version "42.0.0")

(def jdbc-drivers
  ;; 9.4.1207 first version with PgArray
  ;; 9.4-1206 JDBC 41 last release without PgArray
  '#{[org.postgresql/postgresql "42.0.0"]
     [org.postgresql/postgresql "9.4-1201-jdbc41"]
     [org.postgresql/postgresql "9.4-1202-jdbc42"]
     [org.postgresql/postgresql "9.4-1206-jdbc41"]
     [org.postgresql/postgresql "9.4-1206-jdbc42"]})

(deftask with-jdbc
  "Specify non-default JDBC version"
  [v version VAL str "PostgreSQL JDBC version"]
  (let [{:keys [version] :or {version default-jdbc-driver-version}} *opts*]
    (if-let [jdbc (some (fn [[_ v :as d]] (when (= v version) d)) jdbc-drivers)]
      (do (println (format "Adding PostgreSQL JDBC Driver %s" (pr-str jdbc)))
          ;; removing deps doesn't really work, as :dependencies are already
          ;; on the classpath at this point
          (set-env! :dependencies (-> (remove-deps #{'org.postgresql/postgresql})
                                      (conj jdbc))))
      (println (format "Could not find specified JDBC version \"%s\"" version))))
  identity)

;;; code checking

(require '[tolitius.boot-check :as check])

;;; code formatting

(require '[boot-fmt.core :refer [fmt]])

(task-options!
  fmt {:options {:style  :community
                 :extend {:flow?         true
                          :indent        2
                          :nl-separator? true}
                 :fn-map {":import"     :flow
                          ":require"    :flow
                          "and"         :force-nl-body
                          "defprotocol" :arg1-body
                          "merge"       :hang
                          "or"          :force-nl-body
                          "try"         :flow-body}
                 :list   {:indent-arg 1}
                 :map    {:justify? true}
                 :pair   {:justify? true}
                 :set    {:wrap? nil}
                 :vector {:wrap? nil}}
       :files   #{"src"}
       :mode    :diff})

;;;; documentation

(require '[codox.boot :refer [codox]])

(task-options!
  codox {:version      version
         :source-paths (get-env :resource-paths)
         :name         (name project)})

(deftask docs
  "generate html documentation"
  []
  (comp (target) (codox)))
