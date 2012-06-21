(ns tipsy-shell.data
  (:require [clojure.data.json :as j])
  (:import [com.yahoo.chimp.core JSON UUID]
           [com.yahoo.content.core Context Workspace]))

(defn context-uuid [name]
  (-> "ca.auth.backyard" Entity/namespaceId (UUID/fromName name) .stringValue))

;; revs and writers
(defn rev [& _]
  (.. UUID fromCurrentTime stringValue))

(defn writer [& _]
  (context-uuid (cur-user)))

;;
(defn- component [key n] (-> key (.split "\\.") (nth n)))
(defn account [key] (component key 1))
(defn workspace [key] (component key 2))
(defn task [key] (component key 3))
(defn channel [key] (component key 4))


(defmulti as-compact
  "Converts a chimp def. to a compact def. Returns as string"
  (fn [_ key] key))

(defmulti as-chimp
  "Converts a compact def. to chimp definition. Returns as string"
  (fn [_ key] key))

(defmethod as-chimp :workspace
  [data _]
  (let [data (if (string? (j/read-json data)) data)
        ws (get-in data [:workspace])
        namespace (account ws)
        name (workspace ws)
        id (context-uuid ws)]
    {:_id id
     :_schema Context/CONTEXT_SCHEMA
     :facets
     {(str name ":workspace") {}
      (str namespace ":context") {:_namespace   namespace
                                  :access_write [(get-in data [:_writer])]
                                  :_writer      (get-in data [:_writer])
                                  :prefix       ws
                                  :_name        name
                                  :_id          id
                                  :_keys        [ws]
                                  :_facet       (str namespace ":context")
                                  :_context     (context-uuid namespace)}
      (str namespace ":conf") {:workspace ws
                               :queue     (get-in data [:queue])
                               :comment   (get-in data [:comment])
                               :root_dir  (get-in data [:root_dir])
                               :email     (get-in data [:email])
                               :grid_user (get-in data [:grid_user])
                               :_id       id
                               :_facet    (str namespace ":conf")
                               :_context  id
                               :_writer   (get-in data [:_writer])
                               :_rev      (get-in data [:_rev])}}}))

(defmethod as-compact :workspace
  [data _]
  (letfn [(find-namepace [data]
            (let [facet-keys (-> data :facets keys)
                  r (filter #(.endWith % "workspace") facet-keys)]
              (-> r first accountrepl)))]
    (let [data (if (string? (j/read-json data)) data)
          namespace (find-namepace data)
          ws-facet (keyword (str namespace ":workspace"))
          conf-facet (keyword (str namespace ":conf"))
          context-facet (keyword (str namespace ":context"))]
      {:workspace (get-in data [:facets conf-facet :workspace])
       :queue     (get-in data [:facets conf-facet :queue])
       :comment   (get-in data [:facets conf-facet :comment])
       :root_dir  (get-in data [:facets conf-facet :root_dir])
       :email     (get-in data [:facets conf-facet :email])
       :grid_user (get-in data [:facets conf-facet :grid_user])
       :_schema   "ca.tipsy.v1.Workspace"
       :_writer   (get-in data [:facets ws-facet :writer])
       :_rev      (get-in data [:facets conf-facet :_rev])})))

(defn deserialize
  [content type]
  (if (display-chimp)
    content
    (as-compact content type)))

(defn serialize
  [content type]
  (as-chimp content type))
