(ns tipsy-shell.ace
  (:use [tipsy-shell.variables])
  (:require [clojure.data.json :as j])
  (:import [com.yahoo.chimp.core UUID Entity]))

(defn context-uuid [name]
  (-> "ca.auth.backyard" Entity/namespaceId (UUID/fromName name)))

(defn uuid-str [uuid] (.stringValue uuid))

;; revs and writers
(defn rev [& _] (.. UUID fromCurrentTime stringValue))

(defn writer [& _]
  (-> :cur-user read-var context-uuid uuid-str))

;; parsing ace names
(defn- component [key n] (-> key name (.split "\\.") (nth n)))
(defn account [key] (component key 0))
(defn workspace [key] (component key 1))
(defn task [key] (component key 2))
(defn channel [key] (component key 3))
(defn key-namespace [key]
  (let [key (name key)
        i (.lastIndexOf key ".")]
    (subs key 0 i)))

(defmulti as-chimp
  "Converts a compact def. to chimp definition. Returns as string"
  (fn [_ key] key))

;; (defmethod as-chimp :default
;;   [data _] data)
