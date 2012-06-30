(ns tipsy-shell.data.workspace
  (:use [tipsy-shell.data]
        [tipsy-shell.util])
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
    (-> {:_id id
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
                                   :workspace key}}}
        clean-up
        j/json-str)))
