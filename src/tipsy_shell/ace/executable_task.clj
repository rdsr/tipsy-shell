(ns tipsy-shell.ace.executable-task
  (:use [tipsy-shell.ace]
        [tipsy-shell.util])
  (:require [clojure.data.json :as j]
            [clojure.string :as s])
  (:import [java.io File]
           [com.yahoo.chimp.core UUID Entity]
           [com.yahoo.content.core Task Context]))

(defn- filename [path] (-> path File. .getName))

(defn add-fields-internal
  [chimp field-mappings]
  (reduce (fn [_chimp [ks v]] (assoc-in _chimp ks v))
          chimp field-mappings))

(defmulti ^:private add-fields
  "Add type (PIG or OOZIE) specific
fields to chimp strucutre"
  (fn [_ _ exec] (keyword exec)))

(defmethod add-fields
  :pig
  [chimp compact _]
  (let [key (compact :task)
        namespace (-> compact :task key-namespace)]
    (add-fields-internal
     chimp
     {[(str namespace ":task") :behavior :script] (-> :script compact filename)})))

(defmethod add-fields
  :oozie
  [chimp compact _]
  (let [key (compact :task)
        namespace (-> compact :task key-namespace)]
    (add-fields-internal chimp
                         {[(str namespace ":task") :behavior :workflow_path] (-> :workflow_path compact filename)
                          [(str namespace ":task") :behavior :oozie_app_path] (compact :oozie_app_path)})))

(defmethod as-chimp :executable-task
  [compact type]
  (let [exec (compact :exec_type)
        compact (if (string? compact) (j/read-json compact) compact)
        key (compact :task)
        namespace (key-namespace key)
        name (task key)
        cntxt-id (context-uuid namespace)
        cntxt-id-str (uuid-str cntxt-id)
        id (-> cntxt-id (UUID/fromName name) uuid-str)]
    (-> {:_id id
         :_schema Task/TASK_SCHEMA
         :facets
         {(str namespace ":task") {:_context   cntxt-id-str
                                   :_facet     (str namespace ":task")
                                   :_id        id
                                   :_keys      [key]
                                   :_name      name
                                   :_namespace namespace
                                   :_rev       (compact :_rev)
                                   :_schema    "ca.types.v1.Task"
                                   :_writer    (compact :_writer)
                                   :behavior {:inputs    (compact :inputs)
                                              :outputs   (compact :outputs)
                                              :exec_type exec
                                              :lib_dir   "lib"}
                                   :max_segments_per_generation (compact :max_segments_per_generation)
                                   :max_generations_per_channel (compact :max_generations_per_channel)}
          (str namespace ":task-resources") {:_id       id
                                             :_context  cntxt-id-str
                                             :_rev      (rev)
                                             :_facet    (str namespace ":task-resources")
                                             :_schema   "ca.types.v1.TaskResources"
                                             :_writer   (compact :_writer)
                                             :resources []}}}
        (add-fields compact (s/lower-case exec))
        clean-up
        j/json-str)))
