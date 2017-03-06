(ns cleanclaim.read-book
  (:require [dk.ative.docjure.spreadsheet :as sheet]
            [cleanclaim.table-templates :refer :all]
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

(def iterable-config-sheets
  [employee-operating
   employee-capital
   goods-operating
   goods-capital
   equip-operating
   equip-capital
   revenue])

(defn- writable-map
  [claim template]
  {:table-name (:table-name template)
   :items (read-sheet claim template)})

(defn read-book
  [claim]
  (map (partial writable-map claim)
       iterable-config-sheets))
