(ns tipsy-shell.http
  (:use [clojure.data.json]
        [tipsy-shell.util]
        [tipsy-shell.variables])
  (:require [clj-http.client :as client])
  (:import [org.apache.http.client.methods HttpGet]))

(defn GET
  ([uri]
     (GET uri {}))
  ([uri opts]
     (let [url (str (base-url) uri)
           resp (client/get url opts)]
       (-> resp :body (deserialize (:type opts))))))

(defn PUT
  ([uri body]
     (PUT uri body {}))
  ([uri body opts]
     (let [url (str (base-url) uri)]
       (client/put url (assoc opts
                         :body (serialize body (:type opts))l
                         :body-encoding "UTF-8")))))
