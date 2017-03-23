(ns cleanclaim.ui
  (:require [seesaw.core :as s]
            [seesaw.chooser :as c]
            [cleanclaim.read-book :as rb]
            [cleanclaim.write-book :as wb]))

(defn initialize-read
  [{:keys [claimant-id input-path output-path]}]
  (->> (rb/read-book input-path)
       (wb/write-book claimant-id output-path)))

(s/native!)

(def state (atom {}))

(defn fchoose
  "Choose a new file.
  The text of input-label will be changed to the resulting path.
  The state atom's state-key (either :input-path or :output-path) will be set appropriately.
  The final return value is identity because fchoose is called from a handler,
  which expects a function return value. (fchoose exists purely for side effects!)"
  [input-label choose-type state-key]
  (c/choose-file :type choose-type
                 :success-fn
                 (fn file-success [fc file] (let [fname (.getAbsolutePath file)]
                                              (s/text! input-label fname)
                                              (swap! state assoc state-key fname))))
  identity)

(defn make-label
  [my-text & {:keys [text-size font-style] :or {:text-size 12 :font-style :sans-serif}}]
  (s/label :text my-text
           :border 15
           :font (seesaw.font/font :size text-size
                                   :name font-style)
           :h-text-position :left
           :v-text-position :center))

(defn make-wrapping
  [my-text & {:keys [text-size font-style] :or {:text-size 12 :font-style :sans-serif}}]
  (s/text  :text my-text
           :border 15
           :font (seesaw.font/font :size text-size
                                   :name font-style)
           :background "#EEEEEE"
           :editable? false
           :multi-line? true
           :wrap-lines? true))

(def in-file-name (make-wrapping "No input file..."))
(def out-file-name (make-wrapping "No output file..."))
(def input-chooser (s/action :name "Choose input file"
                             :handler (fn [e] (fchoose in-file-name :open :input-path))))
(def output-chooser (s/action :name "Choose output file"
                              :handler (fn [e] (fchoose out-file-name :save :output-path))))
(def claimant-id-input (s/text :columns 5 :halign :right))

(def error-reporting (-> (make-wrapping "1. Enter Claimant ID.\n2. Select input claim.xlsm file.\n3. Select destination file, if any.")
                         (s/config! :size [500 :by 100])))
(defn report-error
  [claimid input-file output-file]
  (str "Error with opening file. Here are the values I saw:\n"
       "Claimant ID: " claimid "\n"
       "Input file: " input-file "\n"
       "Output file: " output-file "\n"
       "Fix any blank values and try again. Remember Claimant ID has to be an integer!"))

(defn dispatch-read
  [state]
  (let [claimid (try (Integer/parseInt (s/text claimant-id-input))
                     (catch Exception e nil))
        input-file (get state :input-path)
        output-file (get state :output-path)]
    (if (some #(= nil %) [claimid input-file output-file])
      (s/config! error-reporting :text (report-error claimid input-file output-file))
      (do (s/config! error-reporting :text "Reading claim!")
          (initialize-read {:claimant-id claimid :input-path input-file :output-path output-file}))))
  identity)

(def go-action (s/action :name "Clean this claim!"
                         :handler (fn [e] (dispatch-read @state))))

(def body-grid (s/grid-panel :columns 3
                             :hgap 15
                             :size [600 :by 150]
                             :items [(make-label "Claimant ID")
                                     (make-label "Input File")
                                     (make-label "Output File")
                                     claimant-id-input
                                     in-file-name
                                     out-file-name
                                     ""
                                     input-chooser
                                     output-chooser]))

(def go-panel (s/flow-panel :align :left
                            :minimum-size [200 :by 100]
                            :items [go-action error-reporting]))

(def container (s/vertical-panel :items [#_title-panel
                                         (s/separator)
                                         body-grid
                                         (s/separator)
                                         go-panel]))

(defn show-frame
  []
  (-> (s/frame :title "Cleanclaim 1.0"
               :content container
               :resizable? false
               :size [700 :by 400]
               :on-close :exit)
      s/show!))

(def render-ui (show-frame))
