(ns tipsy-shell.http
  (:use [clojure.data.json]
        [tipsy-shell.ace]
        [tipsy-shell.variables])
  (:require [clj-http.client :as client])
  (:import [org.apache.http.client.methods HttpGet]))

(defn success? [r]
  (if (map? r)
    (success? (:status r))
    (and (>= r 200) (< r 300))))

(defn- request
  [method uri opts]
  (let [url (-> :base-url read-var (str uri))
        r (client/request
           (merge {:method method :url url :throw-exceptions false}
                  opts))]
    {:status (:status r) :body (:body r)}))

(defn GET
  ([uri] (GET uri {}))
  ([uri opts]
     (let [r (request :get uri opts)]
       (if (success? r) (:body r) r))))

(defn POST [uri body opts]
  (request :post uri (assoc opts :body body)))

(defn PUT [uri body opts]
  (request :put uri (assoc opts :body body)))
