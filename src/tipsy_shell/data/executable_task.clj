(ns tipsy-shell.data.executable-task
  (:use [tipsy-shell.data])
  (:require [clojure.data.json :as j])
  (:import [com.yahoo.chimp.core UUID Entity]
           [com.yahoo.content.core Task Context]))

(defmethod as-chimp :executable-pig-task
  [data _]
  (let [data (if (string? data) (j/read-json data) data)
        key (get data :task)
        namespace (key-namespace key)
        name (task key)
        cntxt-id (context-uuid namespace)
        cntxt-id-str (uuid-str cntxt-id)
        id (-> cntxt-id (UUID/fromName name) uuid-str)]
    (j/json-str
     {:_id id
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
                                           :exec_type :PIG
                                           :script    (get data :script)
                                           :lib_dir   (get data :lib_dir)}}
       (str namespace ":task-resources") {:_id       id
                                          :_context  cntxt-id-str
                                          :_rev      (rev)
                                          :_facet    (str namespace ":task-resources")
                                          :_schema   "ca.types.v1.TaskResources"
                                          :_writer   (get data :_writer)
                                          :resources []}}})))

;; (defmethod as-compact :executable-pig-task
;;   [data _]
;;   (let [data (if (string? data) (j/read-json data) data)
;;         namespace (find-namespace data :task)
;;         task-facet (-> namespace (str ":task") keyword)]
;;     (j/json-str
;;      {:_rev     (get-in data [:facets task-facet :_rev])
;;       :_schema  "ca.tipsy.v1.ImporterTask"
;;       :_writer  (get-in data [:facets task-facet :_rev])
;;       :channels (get-in data [:facets task-facet :channels])
;;       :task     (str namespace (get-in data [:facets task-facet :name]))})))
