(ns cleanclaim.write-configs)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;; CLEANCLAIM WRITE CONFIGS
;;;; 
;;;; The write-config map controls how a claim read from the forms
;;;; into memory (as a map from table names to seqs of individual costs,
;;;; where each cost is a map of columns to values) is transformed
;;;; into a seq of [sheet-names vec-of-cost-vecs].
;;;;
;;;; This is accomplished by mapping :extract-row over expense-seqs in
;;;; the in-memory map.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;
;; HELPER FUNCTIONS
;;;;;;;;;;;;;;;;;;;

(defn- sum-read-sheet
  "Find the sum of values for a certain field in a seq of maps.
  It is safe for table-field to return a nil value."
  [cost-list table-field]
  (let [nilled-cost-list (remove nil?
                                 (map table-field
                                      cost-list))]
    (reduce + nilled-cost-list)))

(defn- sum-write-sheet
  "Find the sum of values for a certain index in a spreadsheet row vec.
  It is safe for idx-for-row-total to return a nil value."
  [cost-list idx-for-row-total]
  (let [nilled-cost-list (remove nil?
                                 (map #(nth % idx-for-row-total)
                                      cost-list))]
    (reduce + nilled-cost-list)))

(defn- expense-class->int
  [expense-class]
  (if (= expense-class :operating)
    1
    2))


;;;;;;;;;;;;;;;;
;; WRITE CONFIGS
;;;;;;;;;;;;;;;;

(def write-config

  ;;;;;;;;;;;
  ;; EMPLOYEE
  ;;;;;;;;;;;

  {"Employee"
   {:header-row
    ["State" "ClaimantID" "ExpenseType" "EmployeeName" "WorkStart" "WorkEnd" "Function" "OTHours" "OverTimeWage" "TotalWageR" "TotalWageTE" "Benefits" "Reference" "ReportedTotal" "AdjustedTotal" "WageType"]

    :read-table-total
    (fn [cost-list]
      (sum-read-sheet cost-list :total))

    :write-table-total
    (fn [cost-vec]
      (sum-write-sheet cost-vec 14))

    :extract-row
    (fn [claimant-id
         {:keys [expense-class name etype start end function ot-hours ot-hourly ot-total te-total benefits reference total]}]
      [1 claimant-id (expense-class->int expense-class) name start end function ot-hours ot-hourly ot-total te-total benefits reference total total etype])}


   ;;;;;;;;;;;;;;;;;;;;;
   ;; GOODS AND SERVICES
   ;;;;;;;;;;;;;;;;;;;;;

   "Goods and Services"
   {:header-row ["State" "ClaimantID" "ExpenseType" "WorkStart" "WorkEnd" "Supplier" "Description" "EligibleCost" "UnrecoverableHSTNormal" "UnrecoverableHSTManual" "ReportedTotal" "AdjustedTotal" "Reference"]

    :read-table-total
    (fn [cost-list]
      (sum-read-sheet cost-list :total))

    :write-table-total
    (fn [cost-vec]
      (sum-write-sheet cost-vec 11))

    :extract-row
    (fn [claimant-id
         {:keys [expense-class start end supplier description eligible-excluding unrecoverable unrecoverable-manual total reference]}]
      [1 claimant-id (expense-class->int expense-class) start end supplier description eligible-excluding unrecoverable unrecoverable-manual total total reference])}


   ;;;;;;;;;;;;
   ;; EQUIPMENT
   ;;;;;;;;;;;;

   "Equipment"
   {:header-row ["State" "ClaimantID" "ExpenseType" "WorkStart" "WorkEnd" "EquipmentActivity" "Classifier" "MakeModel" "HoursUsed" "RentalRate" "RentalTotal" "Reference" "ReportedTotal" "AdjustedTotal"]

    :read-table-total
    (fn [cost-list]
      (sum-read-sheet cost-list :grand-total))

    :write-table-total
    (fn [cost-vec]
      (sum-write-sheet cost-vec 13))

    :extract-row
    (fn [claimant-id
         {:keys [start end equipment classifier make-model hours rate total reference grand-total expense-class]}]
      [1 claimant-id (expense-class->int expense-class) start end equipment classifier make-model hours rate total reference grand-total grand-total])}


   ;;;;;;;;;;
   ;; REVENUE
   ;;;;;;;;;;

   "Revenue"
   {:header-row ["ClaimantID" "StartDate" "EndDate" "Source" "Description" "Income" "Reference"]

    :read-table-total
    (fn [cost-list]
      (sum-read-sheet cost-list :income))

    :write-table-total
    (fn [cost-vec]
      (sum-write-sheet cost-vec 6))

    :extract-row
    (fn [claimant-id
         {:keys [start end source description income reference]}]
      [claimant-id start end source description income reference])}


   ;;;;;;;;;;;;;;;
   ;; FUTURE COSTS
   ;;;;;;;;;;;;;;;

   "Future Costs"
   {:header-row
    ["State" "ClaimantID" "Supplier" "Description" "DocumentationType" "Reference" "TotalEstimated"]

    :read-table-total
    (fn [cost-list]
      (sum-read-sheet cost-list :total))

    :write-table-total
    (fn [cost-vec]
      (sum-write-sheet cost-vec 7))
    
    :extract-row
    (fn [claimant-id
         {:keys [supplier description documentation reference total] }]
      [1 claimant-id supplier description documentation reference total])}})

