(ns cleanclaim.write-book
  (:require [cleanclaim.read-book :as readbook]
            [cleanclaim.write-configs :as config]
            [dk.ative.docjure.spreadsheet :as sheet :refer [load-workbook]]
            [cleanclaim.read-book :as readb]))

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

(defmulti write-sheet
  "Take data for a certain expense type produced by reading an input claim form,
  a config map for writing expense types,
  a key identifying the expense type,
  to produce information to be written to a workbook.
  For each expense type, writable information has the form
  [table-name-as-str [expense-type-header-row-vec & expense-row-vecs]].

  Writable information for each expense is reduced by (apply conj) into a seq
  and passed to save-workbook, to save to disk."
  (fn [_ _ _ table] table))

(defmethod write-sheet "Admin Info"
  ;; Note that there is no iterating for admin info. It's one row per claim.
  [claimant-id input-book write-config admin-info-sheet-name]
  (let [{:keys [extract-row header-row]} (get write-config admin-info-sheet-name)
        admin-sheet (get input-book admin-info-sheet-name)]
    [admin-info-sheet-name
     (vector header-row
             (extract-row claimant-id admin-sheet))]))

(defn- summarize-read-sheet
  "For a single expense category, retrieve # of items read and their total value."
  [input-book write-configs sheet-name]
  (let [expenses-for-this-sheet (get input-book sheet-name)
        total-fn-for-sheet (get-in write-configs [sheet-name :read-table-total])
        [count-of-items total-for-items] (total-fn-for-sheet expenses-for-this-sheet)]
    [sheet-name
     count-of-items
     total-for-items]))

(defmethod write-sheet "Expenses Summary"
  [claimant-id input-book write-configs expense-sheet-name]
  [expense-sheet-name
   (into []
         (cons ["Expense type" "Number of items read" "Total of items read"]
               (map
                (partial summarize-read-sheet input-book write-configs)
                (remove #(= % "Admin Info")
                        (keys input-book)))))])

(defmethod write-sheet :default
  [claimant-id input-book write-config table-key]
  (let [rows (get input-book table-key)
        {:keys [header-row table-total-fn extract-row]} (get write-config table-key)]
    [table-key
     (into []
           (cons header-row
                 (vec (map (partial extract-row claimant-id)
                           rows))))]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PIPELINE FOR WRITING SHEETS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def write-tables
  ["Expenses Summary" "Admin Info" "Goods and Services" "Employee" "Equipment" "Revenue" "Future Costs"])

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

