(ns tipsy-shell.http
  (:use [clojure.data.json]
        [tipsy-shell.data]
        [tipsy-shell.variables])
  (:require [clj-http.client :as client])
  (:import [org.apache.http.client.methods HttpGet]))

(defn success? [code]
  (and (>= code 200) (< code 300)))

(defn- request
  [method uri opts]
  (let [url (-> :base-url read-var (str uri))]
    (client/request (merge {:method method :url url :throw-exceptions false} opts))))

(defn GET [uri opts]
  (let [r (request :get uri opts)]
    (if (success? (:status r))
      (:body r)
      {:status (:status r) :body (:body r)})))

(defn- entity-enc-request
  [method uri body opts]
  (let [r (request method uri (assoc opts :body body))]
    (if (success? (:status r))
      'done!
      {:status (:status r) :body (:body r)})))

(defn POST [uri body opts]
  (entity-enc-request :post uri body opts))

(defn PUT [uri body opts]
  (entity-enc-request :put uri body opts))
