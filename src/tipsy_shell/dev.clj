(ns tipsy-shell.dev
  (:import [java.io File]))

;; dev routines
(defn- required-ns [] ['tipsy-shell.core 'tipsy-shell.channel])

(defn- generate-completions []
  (let [completions (mapcat (comp keys ns-publics) (required-ns))
        f (File. "completions")]
    (spit f (apply str (interpose \newline completions)))))
