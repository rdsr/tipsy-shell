(ns tipsy-shell.ace.executable-task
  (:use [tipsy-shell.ace]
        [tipsy-shell.util])
  (:require [clojure.data.json :as j])
  (:import [java.io File]
           [com.yahoo.chimp.core UUID Entity]
           [com.yahoo.content.core Task Context]))

(defn filename [path] (-> path File. .getName))

(defmethod as-chimp :executable-pig-task
  [data _]
  (let [data (if (string? data) (j/read-json data) data)
        key (get data :task)
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
                                   :_rev       (get data :_rev)
                                   :_schema    "ca.types.v1.Task"
                                   :_writer    (get data :_writer)
                                   :behavior {:inputs    (get data :inputs)
                                              :outputs   (get data :outputs)
                                              :exec_type "PIG"
                                              :script    (filename (get data :script))
                                              :lib_dir   "lib"} ;;TODO check this
                                   :max_segments_per_generation (get data :max_segments_per_generation)
                                   :max_generations_per_channel (get data :max_generations_per_channel)}
          (str namespace ":task-resources") {:_id       id
                                             :_context  cntxt-id-str
                                             :_rev      (rev)
                                             :_facet    (str namespace ":task-resources")
                                             :_schema   "ca.types.v1.TaskResources"
                                             :_writer   (get data :_writer)
                                             :resources []}}}
     clean-up
     j/json-str)))
