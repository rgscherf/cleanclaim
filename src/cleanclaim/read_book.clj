(ns cleanclaim.read-book
  (:require [dk.ative.docjure.spreadsheet :as sheet]
            [cleanclaim.read-configs :as config]
            [cleanclaim.wrap-docjure :as wrap]))


;;;;;;;;;;;;;;;;;;;
;; UTILITY FUNCTIONS
;;;;;;;;;;;;;;;;;;;

(defn- sheet-by-index
  "Retrieve sheet from book by its index in sheet-seq"
  [book read-idx]
  (nth (sheet/sheet-seq book)
       read-idx))

(defn- passes-nilcheck?
  "Checks that EVERY value tagged by nilcheck-cols contains a value on this row."
  [nilcheck-cols this-row]
  (if this-row
    (every? identity
            (map #(this-row %)
                 nilcheck-cols))))

(defn read-cell-at
  "Read the value of a cell on table-sheet."
  [table-sheet location]
  (sheet/read-cell
   (sheet/select-cell location
                      table-sheet)))


;;;;;;;;;;;;;;;;;;
;; GENERIC READING
;;;;;;;;;;;;;;;;;;

(defn- read-sheet
  "Reads a sheet defined by config map. See namespace cleanclaim.read-configs"
  [book {:keys [read-idx sheet-cols drops nilcheck correction expense-class]}]
  (->> (sheet-by-index book read-idx)
       (wrap/select-columns* sheet-cols) ;; select correct cols
       (drop drops) ;; drop required # of rows for this sheet
       (filter #(passes-nilcheck? nilcheck %)) ;; ensure only valid rows
       (map correction) ;; fill in formula values
       (map #(assoc % :expense-class expense-class)))) ;; add :operating/:capital

(defn- writable-map
  "Process a config sheet, concatting the result to the read-book table result
  for this expense type (if it's aready been observed)"
  [claim acc-map {:keys [table-name] :as config-sheet}]
  (assoc acc-map
         table-name
         (concat (get acc-map table-name)
                 (read-sheet claim config-sheet))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SPECIAL READING FOR ADMIN PAGE
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- stringify-field
  "A maybe-needed function for converting a field to string type.
  May be needed for phone numbers."
  [field m]
  (assoc m field (str (field m))))

(defn- read-admin-page
  "Replace each entry in admin-config with the value it points to.
  Ex {:second-email A4} -> {:second-email jsmith@gmail.com}"
  [source-claim admin-conf]
  (let [admin-sheet (sheet-by-index source-claim 0)]
    (->> admin-conf
         keys
         (map (fn associate-admin-value [field]
                {field (read-cell-at admin-sheet
                                     (field admin-conf))}))
         (apply merge)
         #_(stringify-field :treas-phone)
         #_(stringify-field :second-phone))))


;;;;;;;;;;;;;
;; PUBLIC API
;;;;;;;;;;;;;

(defn read-book
  "Process a claim using config maps defined in cleanclaim.read-configs.
  Returns a map string-table-names to seqs-of-items-of-expense-type,
  with operating and capital merged under the same table names.
  After reducing through the generic tables, assoc data from admin info
  (where a constant # of defined cells are read to the write table)."
  [input-path]
  (let [claim (sheet/load-workbook input-path)
        claim-without-admin
        ;; assoc-reduce to build up a map of (lists of) expense classes
        (reduce (partial writable-map claim)
                {}
                config/config-coll)]
    ;; the admin info sheet isn't a seq of expenses, so we assoc individually.
    (assoc claim-without-admin
           "Admin Info"
           (read-admin-page claim config/admin-config))))
