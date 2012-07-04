(ns tipsy-shell.http
  (:use [clojure.data.json]
        [tipsy-shell.data]
        [tipsy-shell.variables])
  (:require [clj-http.client :as client])
  (:import [org.apache.http.client.methods HttpGet]))

(defn success? [r]
  (if (map? r)
    (success? (:code r))
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
     (let [r (request :get uri {:query-params opts})]
       (if (success? r) (:body r) r))))

(defn POST [uri body opts]
  (request :post uri {:query-params opts :body body}))

(defn PUT [uri body opts]
  (request :put uri {:query-params opts :body body}))
