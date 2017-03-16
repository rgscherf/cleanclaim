(ns cleanclaim.write-book
  (:require [cleanclaim.read-book :as readbook]
            [cleanclaim.write-configs :as config]
            [dk.ative.docjure.spreadsheet :as sheet :refer [load-workbook]]))

;;; TODO for write-config-maps
;;; what are the int values of :operating/:capital?
;;; what is the default int value for under-review items?

;; TODO for writing
;; - Write admin-info
;; - Write summary sheet to ensure we got all read/write expenses.


;;;;;;;;;;;;;;;;;
;; WRITING SHEETS
;;;;;;;;;;;;;;;;;
;; ns write-configs contains specs for each table, keyed on the table name.
;; specs are:
;; :header-row -- vec containing table header
;; :extract-table-row -- fn for row-map->row-vec
;; :table-total-fn -- fn for summing grand total of all entries in table

;; NOTE write-sheet is a multimethod because writing to the "Admin Info" sheet
;; is different from iterating through expense items.

(defmulti write-sheet (fn [_ _ _ table] table))

(defmethod write-sheet "Admin Info"
  [& _]
  ["Admin Info" [["Hello" "World"]]])

(defmethod write-sheet :default
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

(def write-tables
  ["Admin Info" "Goods and Services" "Employee" "Equipment" "Revenue" "Future Costs"])

(defn- workbook-name
  "Make the name of the workbook using the municipality's name.
  If the name cannot be found, the current date is used."
  [input-book]
  (str "cleanclaim - "
       (if-let [municipality-name (get-in input-book ["Admin Info" :claimant])]
         municipality-name
         (java.util.Date.))
       ".xlsx"))

(defn- write-book-to-disk
  "Write workbook to disk after it is processed by write-book."
  [book-name write-data]
  (->> write-data
       (apply sheet/create-workbook)
       (sheet/save-workbook! book-name)))

(defn- append-single-sheet
  "Append a single writable sheet to the accumulating vec of writable sheets."
  [claimant-id input-book book-being-built data-for-new-sheet]
  (->> data-for-new-sheet
       (write-sheet claimant-id input-book config/write-config)
       (apply conj book-being-built)))

(defn- build-book-as-vecs
  "Reduce through the input book, building a vec of processed (by append-single-sheet)
  sheets to be written to a workbook."
  [claimant-id input-book write-tables]
  (reduce (partial append-single-sheet claimant-id input-book)
          []
          write-tables))

(defn write-book
  "Given an input book mapping table names to expenses,
  write a book where rows have been organized into vectors.
  Tab ordering is given by order of write-tables."
  [claimant-id input-book]
  (write-book-to-disk (workbook-name input-book)
                      (build-book-as-vecs claimant-id
                                          input-book
                                          write-tables)))

