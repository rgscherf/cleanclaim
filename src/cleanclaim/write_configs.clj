(ns cleanclaim.write-configs)

(defn- sum-table-field
  [cost-list table-field]
  (let [nilled-cost-list (remove nil?
                                 (map table-field
                                      cost-list))]
    (reduce + nilled-cost-list)))

(defn- class->int
  [expense-class]
  (if (= expense-class :operating)
    1
    2))

(def write-config

  ;;;;;;;;;;;
  ;; EMPLOYEE
  ;;;;;;;;;;;

  {"Employee"
   {:header-row
    ["State" "ClaimantID" "ExpenseType" "EmployeeName" "WorkStart" "WorkEnd" "Function" "OTHours" "OverTimeWage" "TotalWageR" "TotalWageTE" "Benefits" "Reference" "ReportedTotal" "AdjustedTotal"]

    :table-total
    (fn [cost-list]
      (sum-table-field cost-list :total))

    :extract-row
    (fn [claimant-id
         {:keys [expense-class name etype start end function ot-hours ot-hourly ot-total te-total benefits reference total]}]
      [1 claimant-id (class->int expense-class) name start end function ot-hours ot-hourly ot-total te-total benefits reference total total etype])}


   ;;;;;;;;;;;;;;;;;;;;;
   ;; GOODS AND SERVICES
   ;;;;;;;;;;;;;;;;;;;;;

   "Goods and Services"
   {:header-row ["State" "ClaimantID" "ExpenseType" "WorkStart" "WorkEnd" "Supplier" "Description" "EligibleCost" "UnrecoverableHSTNormal" "UnrecoverableHSTManual" "ReportedTotal" "AdjustedTotal" "Reference"]

    :table-total
    (fn [cost-list]
      (sum-table-field cost-list :total))

    :extract-row
    (fn [claimant-id
         {:keys [expense-class start end supplier description eligible-excluding unrecoverable unrecoverable-manual total reference]}]
      [1 claimant-id (class->int expense-class) start end supplier description eligible-excluding unrecoverable unrecoverable-manual total total reference])}


   ;;;;;;;;;;;;
   ;; EQUIPMENT
   ;;;;;;;;;;;;

   "Equipment"
   {:header-row ["State" "ClaimantID" "ExpenseType" "WorkStart" "WorkEnd" "EquipmentActivity" "Classifier" "MakeModel" "HoursUsed" "RentalRate" "RentalTotal" "Reference" "ReportedTotal" "AdjustedTotal"]

    :table-total
    (fn [cost-list]
      (sum-table-field cost-list :grand-total))

    :extract-row
    (fn [claimant-id
         {:keys [start end equipment classifier make-model hours rate total reference grand-total expense-class]}]
      [1 claimant-id (class->int expense-class) start end equipment classifier make-model hours rate total reference grand-total grand-total])}


   ;;;;;;;;;;
   ;; REVENUE
   ;;;;;;;;;;

   "Revenue"
   {:header-row ["ClaimantID" "StartDate" "EndDate" "Source" "Description" "Income" "Reference"]

    :table-total
    (fn [cost-list]
      (sum-table-field cost-list :income))

    :extract-row
    (fn [claimant-id
         {:keys [start end source description income reference]}]
      [1 claimant-id start end source description income reference])}


   ;;;;;;;;;;;;;;;
   ;; FUTURE COSTS
   ;;;;;;;;;;;;;;;

   "Future Costs"
   {:header-row
    ["State" "ClaimantID" "Supplier" "Description" "DocumentationType" "Reference" "TotalEstimated"]

    :table-total
    (fn [cost-list]
      (sum-table-field cost-list :total))
    
    :extract-row
    (fn [claimant-id
         {:keys [supplier description documentation reference total] }]
      [1 claimant-id supplier description documentation reference total])}})

