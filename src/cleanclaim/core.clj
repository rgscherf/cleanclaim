(ns cleanclaim.core
  (:require [cleanclaim.ui :as ui])
  (:gen-class))

(defn main
  []
  (ui/render-ui))

(defn -main
  [& args]
  (main))

