(ns cleanclaim.read-configs)

;;;; Templates for MDRA claim sheets

;;; Read-sheet takes a workbook and config template.
;;; Templates contain information about how to read a table,
;;; fields that must not be nil so that a row will be validated,
;;; and a function that fills in calculated fields because docjure
;;;   can't read fomulas. (This fn might just be identity).

;;; fields for reading a table:
;;;   :read-idx - the index of a given sheet in the source book
;;;   :sheet-cols - the columns within that sheet to grab
;;;   :drops - # of cols to drop before user information begins
;;;   :nilcheck - a vec of cells to validate against (where a nil value in any will drop the row),


(defn- sum-nillable
  "Sum a series of values that may be nil."
  [& args]
  (apply + (remove nil? args)))


;;;;;;;;;;;;;;;;;;;;;
;; ADMIN/CONTACT INFO
;;;;;;;;;;;;;;;;;;;;;
;; Locations of relevant fields for the admin-info table.
;; Admin table is not called in the same way as other tables.
;; See ns read-book.
(def admin-config
  {:claimant "F4"
   :event-type "F7"
   :treas-name "F10"
   :treas-email "F13"
   :treas-phone "F16"
   :second-name "F19"
   :second-title "F22"
   :second-email "F25"
   :second-phone "F28"
   :street "AA5"
   :city "AA7"
   :postal "AA9"
   :submitted "AA11"})

;;;;;;;;;;;;;;;;;;;;;
;; GOODS AND SERVICES
;;;;;;;;;;;;;;;;;;;;;

(defn- goods-correction
  "Correction for goods/services operating table because we can't parse forumlas!
  Adjusts unrecoverable HST and Total fields"
  [{:keys [unrecoverable-manual eligible-excluding] :as gso}]
  (let [;; row will contain unrecoverable OR unrecoverable manual
        unrecoverable (if unrecoverable-manual
                        nil
                        (* 0.0176 eligible-excluding))
        ;; filter nil before summing subtotal + unrecoverable + manual
        total (sum-nillable eligible-excluding
                            unrecoverable
                            unrecoverable-manual)]
    (assoc gso
           :unrecoverable unrecoverable
           :total total)))

(def goods-operating
  {:read-idx 1
   :sheet-cols {:C :start
                :D :end
                :E :supplier
                :F :description
                :G :eligible-excluding
                :H :unrecoverable
                :I :unrecoverable-manual
                :J :total
                :K :reference}
   :drops 11
   :nilcheck [:start :end :eligible-excluding]
   :table-name "Goods and Services"
   :expense-class :operating
   :correction goods-correction})

(def goods-capital
  (assoc goods-operating
         :read-idx 2
         :expense-class :capital
         :drops 9))

;;;;;;;;;;;
;; EMPLOYEE
;;;;;;;;;;;

(defn employee-correction
  "Corrects formulas for OT total and grand total."
  [{:keys [etype ot-hourly ot-hours benefits te-total]
    :as emp}]
  (let [ot-total* (if (= etype "R")
                    (* ot-hours ot-hourly)
                    nil)
        total* (if (= etype "R")
                 (sum-nillable ot-total* benefits)
                 te-total)]
    (assoc emp
           :ot-total ot-total*
           :total total*)))

(def employee-operating
  {:read-idx 3
   :sheet-cols {:C :name
                :D :etype
                :E :start
                :F :end
                :G :function
                :H :ot-hours
                :I :ot-hourly
                :J :ot-total
                :K :te-total
                :L :benefits
                :M :reference
                :N :total}
   :drops 12
   :nilcheck [:start :end :name]
   :table-name "Employee"
   :expense-class :operating
   :correction employee-correction})

(def employee-capital
  (assoc employee-operating
         :read-idx 4
         :expense-class :capital
         :drops 13))


;;;;;;;;;;;;
;; EQUIPMENT
;;;;;;;;;;;;

(defn- equip-correction
  [{:keys [hours rate] :as equip}]
  (if-let [total* (* (or hours 0.0)
                     (or rate 0.0)
                     0.4)]
    (assoc equip
           :total total*
           :grand-total total*)))

(def equip-operating
  {:read-idx 5
   :sheet-cols {:C :start
                :D :end
                :E :equipment
                :F :classifier
                :G :make-model
                :H :hours
                :I :rate
                :J :total
                :K :reference
                :L :grand-total}
   :drops 12
   :nilcheck [:start :end :equipment]
   :table-name "Equipment"
   :expense-class :operating
   :correction equip-correction})

(def equip-capital
  (assoc equip-operating
         :read-idx 6
         :expense-class :capital
         :drops 10))


;;;;;;;;;;
;; REVENUE
;;;;;;;;;;

(def revenue
  {:read-idx 7
   :sheet-cols {:B :start
                :C :end
                :D :source
                :E :description
                :F :income
                :G :reference}
   :drops 07
   :nilcheck [:start :end]
   :table-name "Revenue"
   :correction identity})


;;;;;;;;;;
;; FUTURE
;;;;;;;;;;

(def future-costs
  {:read-idx 9
   :sheet-cols {:B :expense-class
                :C :supplier
                :D :description
                :E :documentation
                :F :reference
                :G :total}
   :drops 7
   :nilcheck [:expense-class :description]
   :table-name "Future Costs"
   :correction identity})


;; Collection of all config maps
;; to be mapped through with read-book/read-book.
(def config-coll
  [employee-operating
   employee-capital
   goods-operating
   goods-capital
   equip-operating
   equip-capital
   revenue
   future-costs])
