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
        (expenses-of book expense-type))))

;; TODO
;; (done) retrieve expenses from munged workbook by type
;; (done) get a seq of all expenses for each write table
;; write expenses to table


;; to write to book, all expenses for a sheet need to be in a vec of vecs
;; with nth 0 representing column A, etc
;; final signature will look like ["Employee" [all-employee-costs] "Equipment" [all-equipment-costs]] etc.

(comment
  (def ex (read-book claim))
  (expenses-of ex "Employee")
  (let [names (set (map :table-name ex))]
    (map (partial flatten-expenses-of ex) names))
)


