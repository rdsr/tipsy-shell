(ns tipsy-shell.workspace
  (:use [tipsy-shell.variables]
        [tipsy-shell.data]
        [tipsy-shell.util]
        [tipsy-shell.http])
  (:require [clojure.data.json :as j])
  (:import [java.awt Desktop]))

(defn read-workspaces
  "Returns all the workspaces keys as a
chimp string representation."
  []
  (GET "/ace/v1/workspaces" {:type :workspaces}))

(defn read-workspace
  "Returns as string the compact representation of the workspace having
  'workspace' attribute 'key'"
  [key]
  (GET (str "/ace/v1/workspace/" (name key)) {:type :workspace}))

(def ^:private field-mappings
  {[:grid_user] (read-var :cur-user)
   [:queue]     "default"
   [:email]     (str (read-var :cur-user) "@" (read-var :email-domain))
   [:_rev]      (rev)
   [:_writer]   (writer)})

(defn edit-workspace
  "Allows user to create/modify a workspace. 'key' is the canonical
key of a workspace.  Opens up an editor to edit/create a workspace."
  [key & [fresh]]
  (let [key (name key)
        content (slurp (template key :workspace fresh))
        file (expected-file key :workspace)]
    (spit file content)
    (.edit (Desktop/getDesktop) file)))

(defn put-workspace
  "Makes a put request for a workspace. Returns a http status map"
  [key]
  (let [key (name key)
        file (expected-file key :workspace)
        content (-> file
                    slurp
                    j/read-json
                    (assoc :workspace key) ;; canonical key
                    (add-defaults field-mappings)
                    (as-chimp :workspace))]
    (PUT (str "/ace/v1/workspace/" key)
         content
         {:type :workspace
          :content-type "application/x-data+json"})))
