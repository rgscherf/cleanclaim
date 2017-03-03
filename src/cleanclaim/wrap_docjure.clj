(ns cleanclaim.wrap-docjure
  (:require [dk.ative.docjure.spreadsheet :as s])
  (:import org.apache.poi.ss.util.CellReference
           org.apache.poi.ss.usermodel.Sheet
           org.apache.poi.ss.usermodel.Cell))

;;; This module wraps select-column function from docjure
;;; which throws an error in project-cell when a workbook function cannot be found.
;;; cleanclaim doesn't care about formulas, so we discard exceptions in project-cell*

(defn- project-cell* [column-map ^Cell cell]
  (let [colname (-> cell
                    .getColumnIndex
                    org.apache.poi.ss.util.CellReference/convertNumToColString
                    keyword)
        new-key (column-map colname)]
    (when new-key
      (try 
        {new-key (s/read-cell cell)}
        (catch Exception e
          {new-key nil})))))

(defn select-columns*
  "Takes two arguments: column hashmap and a sheet. The column hashmap
   specifies the mapping from spreadsheet columns dictionary keys:
   its keys are the spreadsheet column names and the values represent
   the names they are mapped to in the result.
   For example, to select columns A and C as :first and :third from the sheet
   (select-columns {:A :first, :C :third} sheet)
   => [{:first \"Value in cell A1\", :third \"Value in cell C1\"} ...] "
  [column-map ^Sheet sheet]
  (s/assert-type sheet Sheet)
  (vec
   (for [row (s/into-seq sheet)]
     (->> (map #(project-cell* column-map %) row)
          (apply merge)))))
