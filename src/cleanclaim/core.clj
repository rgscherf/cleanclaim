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

(comment
  ;; ordering values for write
  (def ex (read-book claim))
  ;; iterate through table names
  (expenses-of ex "Employee")
  (let [names (set (map :table-name ex))]
    (map (partial flatten-expenses-of ex) names))

  )


