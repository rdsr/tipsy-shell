(ns tipsy-shell.util
  (:use [tipsy-shell.variables])
  (:require [clojure.string :as s]
            [clojure.data.json :as j])
  (:import [java.io File]))

;; json indentation
(defn indent [input]
  "Indents json input. Input can be a string or
it can be a json map as returned by j/json-read.
Returns a properly formatted json string."
  (let [input (if (string? input) (j/read-json input) input)]
    (with-out-str
      (j/pprint-json input))))

;; compact definition template utils
(defn fresh-template [type]
  "Returns the fresh template path for filename 'type'.json"
  (let [path (or (System/getenv "templates_path")
                 (-> :compact-defs read-var (str "/templates")))]
    (File. (str path "/" (name type) ".json"))))

(defn expected-file [key type]
  "Returns the expected file path for filename 'key'.json
Creates the necessary parent dirs if missing"
  (let [file (File. (s/join File/separator [(read-var :compact-defs) (read-var :cur-account) (name type) (str (name key) ".json")]))]
    (-> file .getParentFile .mkdirs)
    file))

(defn template [key type fresh]
  "Returns the 'type' template file. 'type' may be
workspace or task. If 'fresh' parameter is specified a
fresh template is returned, else if a file corresponding
to the 'key' exists, that is returned, otherwise a
fresh template is returned"
  (if fresh
    (fresh-template type)
    (let [f (expected-file key type)]
      (if (.exists f)
        f
        (fresh-template type)))))

(defn- unfilled? [value]
  "If a field is blank or begins with a #,
the user has ommitted this."
  (or (s/blank? value) (.startsWith value "#")))

(defn compute-value [default-val]
  "Given a default-val, returns a fn. which
checks whether the user has specified a value
for the field, if not returns the default-val,
else returns the user supplied value."
  (fn [initial-val]
    (if (unfilled? initial-val)
      default-val
      initial-val)))

(defn add-defaults [content field-mappings]
  "Fills in default values for feilds for
which the user didn't provide any default
value. Also remove fields having keys
'general-advice'"
  (letfn [(internal [path value]
            (cond
             (map? value)
             (reduce (fn [r [k v]]
                       (if (= k :_comment) r
                           (assoc r k (internal (conj path k) v))))
                     {} value)
             (vector? value)
             (into [] (map (fn [v] (internal (conj path :i) v)) value))
             :else
             (if-let [f (field-mappings path)] ;; TODO: check for mandator feilds and throw exceptions
                     (f value) value)))]
    (internal [] content)))

(defn fix-path [path]
  (let [path (if (.endsWith path "/")
               (subs path 0 (dec (count path)))
               path)]
    (if (.startsWith path "/") ;; absolute
      path
      (str (read-var :cur-dir) "/" path))))
