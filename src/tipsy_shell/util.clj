(ns tipsy-shell.util
  (:use [tipsy-shell.variables])
  (:require [clojure.string :as s]
            [clojure.data.json :as j])
  (:import [java.io File]
           [com.yahoo.chimp.core JSON]
           [com.yahoo.chimp.core UUID Entity]
           [com.yahoo.tipsy.shell Utility]
           [com.yahoo.tipsy.common.ace TipsyAceUtils]
           [com.yahoo.tipsy.shell.data WorkspaceData TaskData]))

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
  (File. (str (System/getenv "templates_path") "/" (name type) ".json")))

(defn expected-template [key type]
  "Returns the expected template path for filename 'key'.json
Creates the necessary parent dirs if missing"
  (let [file (File. (s/join File/separator [(compact-defs) (cur-account) (name type) (str (name key) ".json")]))]
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

;; (defn uri->type
;;   [uri]
;;   (letfn [(matches [prefix uri] (.startsWith uri prefix))]
;;     (condp matches uri
;;       ;; caution order of clauses matter
;;       "/ace/v1/workspaces"      :workspaces
;;       "/ace/v1/workspace"       :workspace
;;       "/ace/v1/tasks/workspace" :tasks
;;       "/ace/v1/task"            :task
;;       "/ace/v1/channels"        :channels
;;       "/ace/v1/channel"         :channel
;;       "/ace/v1/generations"     :generations
;;       "/ace/v1/segments"        :segments
;;       "/ace/v1/segment"         :segment
;;       "/ace/v1/channel"         :channel
;;       "/ace/v1/triggers"        :triggers
;;       "/ace/v1/executions"      :executions
;;       "/ace/v1/execution"       :execution)))
