(ns cleanclaim.core
  (:require [dk.ative.docjure.spreadsheet :as sheet]))

(comment
  (def sh (sheet/load-workbook "testbook.xlsx"))

  (defn rows-of
    [sheetname]
    (->> sh
         (sheet/select-sheet sheetname)
         sheet/row-seq
         (map #(->> %
                    sheet/cell-seq
                    (map sheet/read-cell)))))

  (rows-of "testsheet-one"))
