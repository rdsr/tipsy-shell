(ns tipsy-shell.workspace
  (:use [tipsy-shell.variables]
        [tipsy-shell.data]
        [tipsy-shell.util]
        [tipsy-shell.http])
  (:require [clojure.data.json :as j])
  (:import [java.awt Desktop]))

(defn read-workspaces []
  "Returns all the workspaces keys as a
chimp string representation."
  (GET "/ace/v1/workspaces" {:type :workspaces}))

(defn read-workspace [key]
  "Returns as string the compact representation of the workspace having
  'workspace' attribute 'key'"
  (GET (str "/ace/v1/workspace/" (name key)) {:type :workspace}))

(def ^:private field-mappings
  {[:grid_user] (compute-value (read-var :cur-user))
   [:queue]     (compute-value "default")
   [:email]     (compute-value (str (read-var :cur-user) "@" (read-var :email-domain)))
   [:comment]   (compute-value "")
   [:_rev]      (compute-value (rev))
   [:_writer]   (compute-value (writer))})

(defn create-workspace [key & [fresh]]
  "Allows user to create/modify a workspace.  'key' is the canonical
key of a workspace.  Opens up an editor to edit/create a workspace."
  (let [key (name key)
        content (slurp (template key :workspace fresh))
        file (expected-file key :workspace)]
    (spit file content)
    (.edit (Desktop/getDesktop) file)))

(defn put-workspace [key]
  "Makes a put request for a workspace. Returns a http status map"
  (let [key (name key)
        file (expected-file key :workspace)
        content (-> file
                    slurp
                    j/read-json
                    (assoc :workspace key) ;; canonical key
                    (add-defaults field-mappings))]
    (PUT (str "/ace/v1/workspace/" key)
         content
         {:type :workspace
          :content-type "application/x-data+json"})))
