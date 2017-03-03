(ns cleanclaim.read-book
  (:require [dk.ative.docjure.spreadsheet :as sheet]
            [cleanclaim.wrap-docjure :as wrap]))

(defn sheet-by-index
  "Retrieve sheet from book by its index in sheet-seq"
  [book idx]
  (nth (sheet/sheet-seq book)
       idx))

(defn- passes-nilcheck?
  "Checks that EVERY value tagged by nilcheck-cols contains a value on this row."
  [nilcheck-cols this-row]
  (if this-row
    (every? identity
            (map #(this-row %)
                 nilcheck-cols))))

(defn read-sheet
  "Reads a sheet defined by config map.
  Config map keys:
    idx - index of worksheet in book.
    sheet-cols - map of excel-label to keyword labels. only take these cols.
    drop-rows - # of rows to drop before user-entered data begins.
    nil-check-col - if this column is nil, no user data on this row."
  [book {:keys [idx sheet-cols drops nilcheck correction]}]
  (->> (sheet-by-index book idx)
       (wrap/select-columns* sheet-cols)
       (drop drops)
       (filter #(passes-nilcheck? nilcheck %))
       (map correction)))

(comment
  (defn readable-rows
    "Given rows in sheet-seq, return a seq of cells in that row."
    [rows]
    (map #(->> %
               sheet/cell-seq
               (map sheet/read-cell))
         rows))

  (defn rows-from-sheet
    "Return a seq (representing rows) of cell-seqs (each representing cells in the parent row) from a sheet object. Preserves nil values."
    [worksheet]
    (->> worksheet
         sheet/row-seq
         readable-rows))

  (defn rows-from-sheetname
    "Return a seq (representing rows) of cell-seqs (each representing cells in the parent row) from a sheet NAME in given workbook. Preserves nil values."
    [book sheetname]
    (->> book
         (sheet/select-sheet sheetname)
         sheet/row-seq
         readable-rows)))

