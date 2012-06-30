(ns tipsy-shell.data.workspace
  (:use [tipsy-shell.data])
  (:require [clojure.data.json :as j])
  (:import [com.yahoo.chimp.core Entity]
           [com.yahoo.content.core Context]))

(defmethod as-chimp :workspace
  [data _]
  (let [data (if (string? data) (j/read-json data) data)
        key (get data :workspace)
        namespace (account key)
        name (workspace key)
        id (-> key context-uuid uuid-str)]
    (j/json-str
     {:_id id
      :_schema Context/CONTEXT_SCHEMA
      :facets
      {(str key ":workspace") {:_context   id
                               :_facet     (str name ":workspace")
                               :_id        id
                               :_keys      [key]
                               :_name      name
                               :_namespace namespace
                               :_writer    (get data :_writer)
                               :tasks      []}
       (str namespace ":context") {:_context     (-> namespace context-uuid uuid-str)
                                   :_facet       (str namespace ":context")
                                   :_id          id
                                   :_keys        [key]
                                   :_name        name
                                   :_namespace   namespace
                                   :_writer      (get data :_writer)
                                   :access_write [(get data :_writer)]
                                   :prefix       key}
       (str namespace ":conf") {:_context  id
                                :_facet    (str namespace ":conf")
                                :_id       id
                                :_rev      (get data :_rev)
                                :_writer   (get data :_writer)
                                :comment   (get data :comment)
                                :email     (get data :email)
                                :grid_user (get data :grid_user)
                                :queue     (get data :queue)
                                :root_dir  (get data :root_dir)
                                :workspace key}}})))

(defmethod as-compact :workspace
  [data _]
  (let [data (if (string? data) (j/read-json data) data)
        namespace (find-namespace data :workspace)
        ws-facet (keyword (str namespace ":workspace"))
        conf-facet (keyword (str namespace ":conf"))
        context-facet (keyword (str namespace ":context"))]
    (j/json-str
     {:_rev      (get-in data [:facets conf-facet :_rev])
      :_schema   "ca.tipsy.v1.Workspace"
      :_writer   (get-in data [:facets ws-facet :_writer])
      :comment   (get-in data [:facets conf-facet :comment])
      :email     (get-in data [:facets conf-facet :email])
      :grid_user (get-in data [:facets conf-facet :grid_user])
      :queue     (get-in data [:facets conf-facet :queue])
      :root_dir  (get-in data [:facets conf-facet :root_dir])
      :workspace (get-in data [:facets conf-facet :workspace])})))
