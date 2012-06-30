(ns tipsy-shell.data.importer-task
  (:use [tipsy-shell.data]
        [tipsy-shell.util])
  (:require [clojure.data.json :as j])
  (:import [com.yahoo.chimp.core UUID Entity]
           [com.yahoo.content.core Task Context]))

(defmethod as-chimp :importer-task
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
                                   :channels   (get data :channels)}
          (str namespace ":task-resources") {:_id id
                                             :_context cntxt-id-str
                                             :_rev (rev)
                                             :_facet (str namespace ":task-resources")
                                             :_schema "ca.types.v1.TaskResources"
                                             :_writer (get data :_writer)
                                             :resources []}}}
        clean-up
        j/json-str)))
