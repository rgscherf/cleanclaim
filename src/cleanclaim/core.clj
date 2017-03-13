(ns cleanclaim.core
  (:require [dk.ative.docjure.spreadsheet :as sheet :refer [load-workbook]]
            [cleanclaim.read-book :as readb]
            [cleanclaim.write-book :as writeb]
            [cleanclaim.table-templates :refer :all]))

(def claim (load-workbook "template-mdra-claim-form--single-cost.xlsm"))

(comment
  (def x (writeb/write-book 10 (read-book claim)))
  (-> claim
      (readb/read-book)
      #(writeb/write-book 10 %))
  )

;; to write to book, all expenses for a sheet need to be in a vec of vecs
;; with nth 0 representing column A, etc
;; final signature will look like ["Employee" [all-employee-costs] "Equipment" [all-equipment-costs]] etc.

