(ns tipsy-shell.http
  (:use [clojure.data.json]
        [tipsy-shell.data]
        [tipsy-shell.variables])
  (:require [clj-http.client :as client])
  (:import [org.apache.http.client.methods HttpGet]))

(defn- request
  [method uri opts]
  (let [url (-> :base-url read-var (str uri))]
    (client/request (merge {:method method :url url} opts))))

(defn GET [uri opts]
  (request :GET uri opts))

(defn- entity-enc-request
  [method uri body opts]
  (request method uri
           (assoc opts
             :body (serialize body (:type opts))
             :body-encoding "UTF-8")))

(defn POST [uri body opts]
  (entity-enc-request :POST uri body opts))

(defn PUT [uri body opts]
  (entity-enc-request :PUT uri body opts))
