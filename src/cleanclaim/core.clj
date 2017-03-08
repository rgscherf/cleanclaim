(ns cleanclaim.core
  (:require [dk.ative.docjure.spreadsheet :as sheet :refer [load-workbook]]
            [cleanclaim.read-book :refer [read-book]]
            [cleanclaim.table-templates :refer :all]))

(def claim (load-workbook "template-mdra-claim-form--single-cost.xlsm"))

(def expenses
  (read-book claim))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; (ns cleanclaim.write-book)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sheets-of
  "Get all sheets in workbook for a (string) expense type,
  as defined in the sheet's :table-name."
  [book table-name]
  (filter #(= (:table-name %) table-name) book))

(defn flatten-expenses-of
  "Flatten items items of multiple tables into one seq."
  [book expense-type]
  (flatten
   (map :items
        (sheets-of book expense-type))))

(defn employee-table-row
  [claimant-id {:keys [expense-class name etype start end function ot-hours ot-hourly ot-total te-total benefits reference total]}]
  (let [operating-to-int #(if (:operating %) 1 2)]
    [1 claimant-id (operating-to-int expense-class) name start end function ot-hours ot-hourly ot-total te-total benefits reference total total etype]))

(defmulti write-sheet
  "Dispatch on string table name (given by :table-name)
  to create a vector of vectors, [[header-row] & [body-rows]]
  for writing to workbook."
  (fn [_ _ n] n))

(defmethod write-sheet "Employee"
  [claimant-id book _]
  (let [rows (flatten-expenses-of book "Employee")
        header-row ["State" "ClaimantID" "ExpenseType" "EmployeeName" "WorkStart" "WorkEnd" "Function" "OTHours" "OverTimeWage" "TotalWageR" "TotalWageTE" "Benefits" "Reference" "ReportedTotal" "AdjustedTotal"]]
    (into []
     (cons
      header-row
      (vec (map (partial employee-table-row claimant-id)
                rows))))))

(comment
  (def x (read-book claim))
  (write-sheet 10 ex "Employee"))

;; to write to book, all expenses for a sheet need to be in a vec of vecs
;; with nth 0 representing column A, etc
;; final signature will look like ["Employee" [all-employee-costs] "Equipment" [all-equipment-costs]] etc.

(comment
  (write-sheet 10 ex "Employee")
  ;; the goal is to get to:
  (def write-sheets ["Goods and Services" "Employee" "Equipment" "Revenue" "Future Costs"])
  (let [sheets (map (partial write-sheet 10 ex) write-sheets)])
  (let [wb (sheet/create-workbook "Employee" (write-sheet 10 ex "Employee"))]
    (sheet/save-workbook! "test2.xlsx" wb)))

