(require '[net.cgrand.enlive-html :as html])
(require '[clojure.string :as str])
(import (java.net URL))
(def appendix-url "https://www.postgresql.org/docs/9.6/static/errcodes-appendix.html")
(def appendix (html/html-resource (URL. appendix-url)))

;; <div class="TABLE">
;;    <a name="ERRCODES-TABLE" id="ERRCODES-TABLE"></a>

;;    <p class="c2">Table A-1. <span class=
;;    "PRODUCTNAME">PostgreSQL</span> Error Codes</p>

;;    <table border="1" class="CALSTABLE">
;;      <col width="1*" title="errorcode" />
;;      <col width="1*" title="condname" />

;;      <thead>
;;        <tr>
;;          <th>Error Code</th>

;;          <th>Condition Name</th>
;;        </tr>
;;      </thead>

;;      <tbody>
;;        <tr>
;;          <td colspan="2"><span class="bold EMPHASIS c3">Class 00 —
;;          Successful Completion</span></td>
;;        </tr>

;;        <tr>
;;          <td><tt class="LITERAL">00000</tt></td>

;;          <td><tt class="SYMBOL">successful_completion</tt></td>
;;        </tr>

(defn row->vec [row]
  (let [[code condition] (->> (html/select row [:td :tt])
                              (map html/text))
        kebab #(str/replace % "_" "-")]
    (when code
      [(keyword code) (-> condition kebab keyword)])))

(defn parse-conditions [page]
  (->>
    (html/select page [:tbody :tr])
    (map row->vec)
    (remove #(not (seq %)))
    (into (sorted-map))))

(def conditions (parse-conditions appendix))

(spit "resources/conditions.edn"
      (with-out-str (clojure.pprint/pprint conditions)))
