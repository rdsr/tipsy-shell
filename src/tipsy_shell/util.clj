(ns tipsy-shell.util
  (:use [tipsy-shell.variables])
  (:require [clojure.string :as s]
            [clojure.data.json :as j]
            [clojure.pprint :as pp])
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

(defn- optional? [value]
  "If a field is blank or begins with a #,
the user has ommitted this."
  (or (s/blank? value) (.startsWith value "#O")))

(defn- mandatory? [value]
  (and (not (s/blank? value)) (.startsWith value "##")))

(defn add-defaults [content field-mappings]
  "Fills in default values for feilds for
which the user didn't provide any default
value."
  (letfn [(internal [path value]
            (cond
             (map? value)
             (reduce (fn [r [k v]]
                       (assoc r k (internal (conj path k) v)))
                     {} value)
             (vector? value)
             (into [] (map (fn [v] (internal (conj path :i) v)) value))
             :else (cond (mandatory? value)
                         (throw (IllegalArgumentException.
                                 (str "Please fill the mandatoy value " value)))
                         (and (optional? value)
                              (contains? field-mappings path))
                         (field-mappings path)
                         :else value)))]
    (internal [] content)))

(defn clean-up [chimp]
  "Removes nils and unfilled (optional) fields from chimp structure"
  (cond
   (map? chimp)
   (reduce (fn [r [k v]]
             (let [v (clean-up v)]
               (if (or (= v :_optional) (= k :_comment))
                   r
                   (assoc r k (clean-up v)))))
             {} chimp)
   (vector? chimp)
   (into [] (map clean-up chimp))
   :else
   (if (optional? chimp)
     :_optional
     chimp)))

(defn fix-path [path]
  (let [path (if (.endsWith path "/")
               (subs path 0 (dec (count path)))
               path)]
    (if (.startsWith path "/") ;; absolute
      path
      (str (read-var :cur-dir) "/" path))))
