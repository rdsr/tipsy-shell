(ns tipsy-shell.core
  (:use [clojure.repl :only (doc)])
  (:use [tipsy-shell.variables])
  (:use [tipsy-shell.util])
  (:use [tipsy-shell.workspace])
  (:use [tipsy-shell.data])
  (:use [tipsy-shell.data.workspace])
  (:use [tipsy-shell.data.importer-task])
  (:use [tipsy-shell.data.executable-task])
  (:require [clojure.java.shell :as s]))

;; TODO define as macro
(defn sh [& args]
  (let [{:keys [exit out err]} (s/sh args)]
    (if (= exit 0) (println out) (println err))
    'done!))
