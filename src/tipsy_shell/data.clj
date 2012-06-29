(ns tipsy-shell.data
  (:use [tipsy-shell.variables])
  (:require [clojure.data.json :as j])
  (:import [com.yahoo.chimp.core UUID Entity]))

(defn context-uuid [name]
  (-> "ca.auth.backyard" Entity/namespaceId (UUID/fromName name) .stringValue))

;; revs and writers
(defn rev [& _]
  (.. UUID fromCurrentTime stringValue))

(defn writer [& _]
  (context-uuid (read-var :cur-user)))

;; parsing ace names
(defn- component [key n] (-> key name (.split "\\.") (nth n)))
(defn account    [key] (component key 0))
(defn workspace  [key] (component key 1))
(defn task       [key] (component key 2))
(defn channel    [key] (component key 3))

;; data conversion routines
(defmulti as-compact
  "Converts a chimp def. to a compact def. Returns as string"
  (fn [_ key] key))

(defmulti as-chimp
  "Converts a compact def. to chimp definition. Returns as string"
  (fn [_ key] key))


(defmethod as-compact :default
  [data _] data)

(defmethod as-chimp :default
  [data _] data)

(defn deserialize
  [content type]
  (if (read-var :display-chimp)
    content
    (as-compact content type)))

(defn serialize
  [content type]
  (as-chimp content type))

(defn find-namespace [data key]
  (let [facet-keys (-> data :facets keys)
        r (filter #(-> % name (.endsWith (name key)) facet-keys))
        key (first r)]
    (get-in data [:facets key :_namespace])))