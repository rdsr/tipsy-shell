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

(defn expected-template [key type]
  "Returns the expected template path for filename 'key'.json
Creates the necessary parent dirs if missing"
  (let [file (File. (s/join File/separator [(read-var :compact-defs) (read-var :cur-account) (name type) (str (name key) ".json")]))]
    (-> file .getParentFile .mkdirs)
    file))

(defn template [key type fresh]
  "Returns the 'type' template file. 'type' may be workspace | task |
channel. If 'fresh' parameter is specified a fresh template is
returned. Else if a template corresponding to the 'key' exists that
is returned, otherwise a fresh template is returned"
  (if fresh
    (fresh-template type)
    (let [f (expected-template key type)]
      (if (.exists f)
        f
        (fresh-template type)))))

(defn default? [value]
  (or (s/blank? value) (.beginsWith value "#")))

(defn add-field [f initial-val]
  (if (default? initial-val)
    (if (fn? f)
      (f initial-val)
      f)
    initial-val))

(defn add-fields [content key fields]
  "Adds key to content, also adds all
fields which can be auto-generated,
fields is a map containing mapping
from path/to/field in content to fn
which would generat the field's value."
  (reduce (fn [content [path func]]
            (update-in content path func))
          content fields))

(defn remove-defaults
  [content]
  "Returns a structure where all string
values which begin with '#' are removed"
  (reduce (fn [content [key value]]
            (if (string? value)
              (if (default? value) (dissoc content key) content)
              (assoc content key (remove-defaults value))))
          content))
