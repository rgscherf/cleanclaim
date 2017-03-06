(ns cleanclaim.table-templates)

;;; Templates for MDRA claim sheets
;;; read-sheet takes a workbook and config template
;;; templates contain:
;;;   :idx - the index of a given sheet,
;;;   :sheet-cols - the columns within that sheet to grab,
;;;   :drops - # of cols to drop before user information begins,
;;;   :nilcheck - a vec of cells to validate against (where a nil value in any will drop the row),
;;;   :correction - and a function with which to construct missing data.

;;; NOTE: DOCJURE CANNOT READ TABLE FORMULAS. Instead, we drop erroring values.
;;; Read-sheet always runs the config map's correction fn to re-add these cols.


(defn- sum-nillable
  "Sum a series of values that may be nil."
  [& args]
  (apply + (remove nil? args)))

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
  {:idx 3
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
   :correction employee-correction})

(def employee-capital
  (assoc employee-operating
         :idx 4
         :drops 13))

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
  {:idx 1
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
   :correction goods-correction})

(def goods-capital
  (assoc goods-operating
         :idx 2
         :drops 9))

;; EQUIPMENT

(defn- equip-correction
  [{:keys [hours rate] :as equip}]
  (if-let [total* (* (or hours 0.0)
                     (or rate 0.0)
                     0.4)]
    (assoc equip
           :total total*
           :grand-total total*)))

(def equip-operating
  {:idx 5
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
   :correction equip-correction})

(def equip-capital
  (assoc equip-operating
         :idx 6
         :drops 10))

;; REVENUE
(def revenue
  {:idx 7
   :sheet-cols {:B :start
                :C :end
                :D :source
                :E :description
                :F :income
                :G :reference}
   :drops 07
   :nilcheck [:start :end]
   :correction identity})

;; FUTURE

(def future-costs
  {:idx 9
   :sheet-cols {:B :item-type
                :C :supplier
                :D :description
                :E :documentation
                :F :reference
                :G :total}
   :drops 07
   :nilcheck [:item-type :description]
   :correction identity})
