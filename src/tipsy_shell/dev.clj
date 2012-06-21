(ns tipsy-shell.dev
  (:import [java.io File]))

;; dev routines
(defn- required-ns []
  (filter #(-> % str (.contains "tipsy")) (all-ns)))

(defn- generate-completions []
  (let [completions (mapcat (comp keys ns-publics) (required-ns))
        f (File. ".completions")]
    (spit f (apply str (interpose \newline completions)))))
