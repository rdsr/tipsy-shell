(ns tipsy-shell.dev
  (:import [java.io File]))

;; dev routines
(defn- required-ns []
  ['tipsy-shell.api.execution
   'tipsy-shell.api.channel
   'tipsy-shell.api.task
   'tipsy-shell.api.workspace
   'tipsy-shell.core])

(defn- generate-completions []
  (let [completions (mapcat (comp keys ns-publics) (required-ns))
        f (File. "completions")]
    (spit f (apply str (interpose \newline completions)))
    (str "Wrote file " (.getAbsolutePath f))))
