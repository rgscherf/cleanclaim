(ns cleanclaim.write-book
  (:require [cleanclaim.read-book :as readbook]
            [cleanclaim.write-configs :as config]
            [dk.ative.docjure.spreadsheet :as sheet :refer [load-workbook]]))

;;; TODO for write-config-maps
;;; what are the int values of :operating/:capital?
;;; what is the default int value for under-review items?


;;;;;;;;;;;;;;;;;
;; WRITING SHEETS
;;;;;;;;;;;;;;;;;
;; ns write-configs contains specs for each table, keyed on the table name.
;; specs are:
;; :header-row -- vec containing table header
;; :extract-table-row -- fn for row-map->row-vec
;; :table-total-fn -- fn for summing grand total of all entries in table

(defn- write-sheet
  [claimant-id input-book write-config table-key]
  (let [rows (get input-book table-key)
        {:keys [header-row table-total-fn extract-row]} (get write-config table-key)]
    [table-key
     (into []
           (cons
            header-row
            (vec (map (partial extract-row claimant-id)
                      rows))))]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PIPELINE FOR WRITING SHEETS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- unpack-pairs
  "Flattens a seq of pairs by one level."
  [seq-of-pairs]
  (reduce (fn [acc pair] (concat acc pair))
          []
          seq-of-pairs))

(def write-tables
  ["Goods and Services" "Employee" "Equipment" "Revenue" "Future Costs"])

(defn make-workbook
  [write-data]
  (->> write-data
       (apply sheet/create-workbook)
       (sheet/save-workbook! "testme.xlsx")))

(defn write-book
  "Given an input book mapping table names to expenses,
  write a book where rows have been organized into vectors.
  Tab ordering is given by order of write-tables."
  [claimant-id input-book]
  (->> write-tables
       (map (partial write-sheet claimant-id input-book config/write-config))
       unpack-pairs
       make-workbook))

(comment
  (def claim (load-workbook "template-mdra-claim-form--single-cost.xlsm"))
  ((readbook/read-book claim) "Future Costs")
  (write-book 10 (readbook/read-book claim)))
