(ns cleanclaim.read-book
  (:require [dk.ative.docjure.spreadsheet :as sheet]
            [cleanclaim.table-config :as config]
            [cleanclaim.wrap-docjure :as wrap]))

(defn- sheet-by-index
  "Retrieve sheet from book by its index in sheet-seq"
  [book read-idx]
  (nth (sheet/sheet-seq book)
       read-idx))

(defn- passes-nilcheck?
  "Checks that EVERY value tagged by nilcheck-cols contains a value on this row."
  [nilcheck-cols this-row]
  (if this-row
    (every? identity
            (map #(this-row %)
                 nilcheck-cols))))

(defn- read-sheet
  "Reads a sheet defined by config map. See namespace cleanclaim.table-templates"
  [book {:keys [read-idx sheet-cols drops nilcheck correction expense-class]}]
  (->> (sheet-by-index book read-idx)
       (wrap/select-columns* sheet-cols) ;; select correct cols
       (drop drops) ;; drop required # of rows for this sheet
       (filter #(passes-nilcheck? nilcheck %)) ;; ensure only valid rows
       (map correction) ;; fill in formula values
       (map #(assoc % :expense-class expense-class)) ;; add :operating/:capital
       ))


(defn- writable-map
  "Process a config sheet, concatting the result to the read-book table result
  for this expense type (if it's aready been observed)"
  [claim acc-map {:keys [table-name] :as config-sheet}]
  (assoc acc-map
         table-name
         (concat (get acc-map table-name)
                 (read-sheet claim config-sheet))))

(defn read-book
  "Process a claim using config maps defined in cleanclaim.config-maps.
  Returns a map string-table-names to seqs-of-items-of-expense-type,
  with operating and capital merged under the same table names."
  [claim]
  (reduce (partial writable-map claim)
          {}
          config/config-coll))

