# Pajamas: Comfy Clojure for PostgreSQL and JDBC

**NOTE: This is alpha. The API is not stable.**

Pajamas provides three nice features:

Easy type conversion:

```clojure
(require '[com.grzm.pajamas.alpha.types.common-types])
```

Convenient query shorthands:

```clojure
(require '[com.grzm.pajamas.alpha.query :as q])

(q/row spec [(q/str "SELECT user_display_name, user_last_logged_in_at"
                    "FROM my.users"
                    "WHERE (user_id, passhash) = (?, ?)")
             user-id passhash])
;; {:user_display_name "grzm",
    :user_last_logged_in_at #inst "2017-07-12T21:20:29.018-00:00"}
```

Informative exceptions:

```clojure
(require
  '[com.grzm.pajamas.alpha.query :as q]
  '[com.grzm.pique.alpha.jdbc :as env])

(try
  (q/query (env/spec) ["SELECT 1 / ?" 0])
  (catch Exception e
    (ex-data e)))
;; => {:message "ERROR: division by zero",
       :sql-state "22012",
       :sql "SELECT 1 / ?",
       :params (0),
       :grzm.pajamas.error/condition :grzm.pajamas.error.conditions/division-by-zero,
       :grzm.pajamas.error/server-error-message {:SQLState "22012",
                                                 :internalPosition 0,
                                                 :severity "ERROR",
                                                 :position 0,
                                                 :message "division by zero"}}
```

## Releases and dependency information

[Leiningen][lein]/[Boot][boot] dependency information:

    [com.grzm/pajamas.alpha "0.1.0-SNAPSHOT"]

[lein]: https://leiningen.org
[boot]: http://boot-clj.com

----

## Usage

### pajamas.alpha.types

It's common in Clojure to use simple, common, transaprent data
structures such as vectors and maps instead of specific
classes. PostgreSQL has a rich type system, and its `json` and `jsonb`
types make it convenient to persist Clojure data structures. JDBC's
`IResultSetReadColumn` and `ISQLParameter` protocols make it convenient to
provide seamless conversion between PostgreSQL and Clojure data types.

Type conversion is à la carte: choose only as much magic as you
want. Want Clojure maps to be interpreted as `jsonb` in PostgreSQL?

```clojure
(require '[com.grzm.pajamas.alpha.types.sql-parameter.map]) ;; Handle maps.
(require '[com.grzm.pajamas.alpha.types.jsonb.map]) ;; Handle maps as `jsonb`.
(require '[com.grzm.pajamas.alpha.types.jsonb.read-column]) ;; read `jsonb`
```

Want Clojure vectors and seqs interpreted as PostgreSQL arrays, and
read them as vectors?

```clojure
(require '[com.grzm.pajamas.alpha.types.sql-parameter.vector]) ;; Handle vectors
(require '[com.grzm.pajamas.alpha.types.array.vector]) ;; Handle vectors as arrays
(require '[com.grzm.pajamas.alpha.types.array.seq]) ;; Handle seqs as arrays
(require '[com.grzm.pajamas.alpha.types.array.vector.read-column]) ;; Read arrays as vectors
```

You can also take advantage of the PreparedStatement metadata by
requiring the `sql-parameter-type.*` namespaces rather than
`sql-parameter.*`.

```clojure
(require '[com.grzm.pajamas.alpha.types.sql-parameter-type.vector])
```

You can mix and match these as you see fit. If you want to write all
vectors as arrays, but want to interpret maps as `jsonb` or `hstore`
based on the statement:

```clojure
(require '[com.grzm.pajamas.alpha.types.sql-parameter.vector])
(require '[com.grzm.pajamas.alpha.types.array.vector]) ;; convert all vectors to arrays

(require '[com.grzm.pajamas.alpha.types.sql-parameter-type.map]) ;; convert maps based on metadata
(require '[com.grzm.pajamas.alpha.types.hstore.map]) ;; convert maps to hstore as appropriate
(require '[com.grzm.pajamas.alpha.types.jsonb.map]) ;; convert maps to jsonb as appropriate
```

#### PostgreSQL arrays

The type mapping between Clojure and PostgreSQL leaves some concepts
up to interpretation. For example, are PostgreSQL arrays better
represented in Clojure as seqs or vectors? We can write either--or
both--to PostgreSQL, but we need to choose one when reading
back. Pajamas provides . If you want to read arrays as vectors:

```clojure
(require '[com.grzm.pajamas.alpha.types.array.vector.read-column])
```

As seqs:

```clojure
(require '[com.grzm.pajamas.alpha.types.array.seq.read-column])
```

Note reading is independent of writing. There's no issue with the following:

```clojure
(require '[com.grzm.pajamas.alpha.types.array.seq]) ;; write seqs as arrays
(require '[com.grzm.pajamas.alpha.types.array.vector]) ;; write vectors as arrays
```

When Pajamas is coercing data to PostgreSQL arrays without metadata,
it makes a best effort attempt by inspecting the first element of the
Clojure data to determine the PostgreSQL array type. If the type can't
be determined, it assumes `text`. If you want to take advantage of the
PreparedStatement metadata, require `types.sql-parameter-type.vector`
or `types.sql-parameter-type.seq` as appropriate.

#### json and jsonb

`pajamas.types` uses [Cheshire][chesire] for parsing and generating
JSON and keywordizes keys when reading data from PostgreSQL. This
means Clojure data with string keys will not round-trip.

[cheshire]: https://github.com/dakrone/cheshire

#### enums

##### Schema-qualified enums vs ResultSet metadata

The PostgreSQL JDBC driver
[doesn't always return the schema name of enum types in the ResultSetMetaData,](#type-info-cache-bug)
so take that into account when designing your schema or your queries
if you want to take advantage of using PostgreSQL type information to
coerce data between PostgreSQL and Clojure.  For example, aliasing the
return column can provide another channel of metadata to use to
distinguish between enum type names that differ only by namespace.

<a id="type-info-cache-bug">
At least as of version PostgreSQL JDBC 42.1.3. If you'd like to know
the details, take a look at the differences in caching between
[`TypeInfoCache.getPGType(String)`][get-pg-type-str] and
[`TypeInfoCache.getPGType(int)`][get-pg-type-int] The short version is
that if `getPGType(String)` populates the cache, as happens with
`PGPreparedStatement.setObject` or `PGConnection.createArrayOf`,
you're going to cache an unqualified version of the type name.

[get-pg-type-str]: https://github.com/pgjdbc/pgjdbc/blob/cb3995b5a0311a2f5f7737fdfe83457680305efb/pgjdbc/src/main/java/org/postgresql/jdbc/TypeInfoCache.java#L359-L384
[get-pg-type-int]: https://github.com/pgjdbc/pgjdbc/blob/cb3995b5a0311a2f5f7737fdfe83457680305efb/pgjdbc/src/main/java/org/postgresql/jdbc/TypeInfoCache.java#L386-L436


#### One-to-one type mapping

Datastructures such as PersistentVector, PersistentArrayMap, and
LazySeq aren't converted automatically to and from PostgreSQL. If you
are mapping each Clojure type to a type in PostgreSQL, you can use
straightforward .

#### One-to-many type mapping

It's also possible to dynamically map a single Clojure data type to
multiple PostgreSQL data types. For example, if you're using both
`jsonb` and `hstore` in PostgreSQL, you can map IPersistentMap to both
of them by using the metadata associated with the JDBC
PreparedStatement to look up the parameter type.

[pgsql-arrays]: https://www.postgresql.org/docs/9.6/static/arrays.html

#### Basic types require no special handling

Basic types such as strings, integers, doubles, booleans, as well byte
array (PostgreSQL bytea) uuid work out of the box with Clojure JDBC
and the PostgreSQL drivers: Pajamas is not needed for these.

 Clojure        | PostgreSQL              
 ---------------|---
 nil            | null
 string         | text, varchar
 integer        | int, bigint
 float          | float, double precision
 boolean        | boolean
 byte-array     | bytea
 java.util.UUID | uuid


```clojure
(require '[com.grzm.pajamas.alpha.types.all])
```

`clojure.lang.IPersistentVector`, `clojure.lang.IPersistentMap`,
`clojure.lang.ISeq` will also dispatch on the target PostgreSQL type. This
means JDBC will make a request to fetch the metadata associated with the
statement.

#### Parameter type lookup

JDBC can also inspect the types of the parameters in the prepared
statement. To do so, JDBC queries the server to parse the statement.

There are a couple of caveats when using: When PostgreSQL can't
determine the parameter types, it throws an exception. You can tell
PostgreSQL the types you expect by using `CAST` (or the
PostgreSQL-specific `::` syntax). Also, the code is making an
additional network request.


##### Caveats

When using JSON, maps with string keys won't round-trip, as the keys are
keywordized when parsed.

Pajamas doens't dispatch on primitive types such a strings, integers,
and booleans by design. This allows JDBC and Postgres to infer these
types when using them in the common case when using them for their
corresponding PostgreSQL values. This has the consequence that, while
valid JSON values, such scalar values will continue to raise
`PSQLException` as Pajamas.

In practice, this shouldn't be much of a hindrance, as JSON is
generally used to encode structured data as opposed to scalar
values. Scalar values can still be written to PostgreSQL by
serializing them manually.

----

### pajamas.alpha.error

Sometimes things go wrong. PostgreSQL provides a lot of information in
its exceptions, and Pajamas provides convenient access to the server
error message and error condition.

```clojure
(require '[com.grzm.pajamas.alpha.error :as error])

(try
   (jdbc/query (env/spec) "SELECT 1 / 0")
   (catch org.postgresql.util.PSQLException e
   {:server-error-message (error/server-error-message e)
    :condition (error/condition e)}))
;; => {:server-error-message {:SQLState "22012",
                              :internalPosition 0,
                              :severity "ERROR",
                              :position 0,
                              :message "division by zero"},
       :condition :com.grzm.pajamas.error.conditions/division-by-zero}
```

The `error/condition` function takes a SQL state code as a string or
keyword as well as an exception:

```clojure
(error/condition "22012")
;; => :grzm.pajamas.error.conditions/division-by-zero

(error/condition :22012)
;; => :grzm.pajamas.error.conditions/division-by-zero

```


----

### pajamas.alpha.query

The `pajamas.query` namespace provides shorthands for common query
options, saving you the ceremony of unwrapping the result set. Clojure
JDBC provides a great interface for providing such options succinctly,
and `pajamas/query` makes these cases even more concise.

A single value:

```clojure
(require '[com.grzm.pajamas.alpha.query :as q])

(def spec (env/spec))

(q/val spec "SELECT 42")
;; => 42
```

A single column:     

```clojure
(q/ary spec "VALUES (1), (2), (3)")
;; => (1, 2, 3)
```                        

A single row:

```clojure
(q/row spec "SELECT 1 AS one, 2 AS two")
;; => {:one 1, :two 2}
```

And, for completeness, an unsullied result set:

```clojure
(q/set spec "(VALUES (3, 4), (1, 2)) AS _ (one, two)")
;; => ({:one 3, :two 4}, {:one 1, :two 2})
```
     
These shorthands wrap `jdbc/query` and additional options are merged.

```clojure
(q/row spec "SELECT 1 AS one, 2 AS two", {:identifiers str/upper-case})
;; => {:ONE 1, :TWO 2}
```

The `aryv` and `setv` variants of `ary` and `set` return vectors
instead of sequences.

```clojure
(q/aryv spec "VALUES (1), (2), (3)")
;; => [1, 2, 3]

(q/setv spec "(VALUES (3, 4), (1, 2)) AS _ (one, two)")
;; => [{:one 3, :two 4}, {:one 1, :two 2}]

```

These shorthand functions wrap any `jdbc/query` `PSQLException`
with `ExceptionInfo` including the `::err/server-error-message` and
`::err/condition`. `q/query` and `q/execute!` provide this same
`ExceptionInfo` and otherwise behave like their JDBC counterparts.


#### `query/str` helper

The helper function `query/str` joins its arguments with spaces, a
shorthand for using `clojure.string/join`. The following are
equivalent:

```clojure
(require '[com.grzm.pajamas.alpha.query :as q])

(q/str "SELECT user_name"
       "FROM my.users"
       "WHERE (user_id, passhash) = (?, ?)")

(require '[clojure.string :as str])

(str/join " " ["SELECT user_name"
               "FROM my.users"
               "WHERE (user_id, passhash) = (?, ?)"])
```

---

## Testing Pajamas

The PostgreSQL JDBC driver has changed over recent versions (in
particular, the array class).  Pajamas works with the recent versions
it's been tested with. If you run into an issue with the version of
the driver you're using, please let me know by opening an issue.

Pajamas testing uses `(env/spec)` to provide the database connection
info. Just set the appropriate environment parameters and you're good
to go.

To run the tests with the PostgreSQL driver specified in the dependencies:

    boot test

If you want to specify a driver version, **comment out the PostgreSQL
driver in the dependencies** and specify the driver version using
`with-jdbc -v VERSION`. For example:

    boot with-jdbc -v 9.4-1206-jdbc4 test

It's useful to confirm the behavior of Clojure JDBC and PostgreSQL independent
of Pajamas type coercions. To do so, add `no-ext` to the boot command.

    boot no-ext test
    
Testing the value-only ISQLParameter implementations:

    boot with-values test

## License

© 2017 Michael Glaesemann

Distributed under the MIT License. See LICENSE for details.
