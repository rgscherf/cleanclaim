(ns cleanclaim.core
  (:require [dk.ative.docjure.spreadsheet :as sheet :refer [load-workbook]]
            [cleanclaim.read-book :as readb]
            [cleanclaim.write-book :as writeb]
            ))

(def claim (load-workbook "template-mdra-claim-form--single-cost.xlsm"))

(comment
  (->> claim
      readb/read-book
      (writeb/write-book 10)))
