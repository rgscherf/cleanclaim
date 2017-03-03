(ns cleanclaim.core
  (:require [dk.ative.docjure.spreadsheet :refer [load-workbook]]
            [cleanclaim.read-book :refer [read-sheet]]
            [cleanclaim.table-templates :refer :all]))

(def claim (load-workbook "template-mdra-claim-form--single-cost.xlsm"))

(defn writable-map
  [template]
  {:book-index (:idx template)
   :items (read-sheet claim template)})

(map writable-map
     [employee-operating
      employee-capital
      goods-operating
      goods-capital
      equip-operating
      equip-capital
      revenue])

