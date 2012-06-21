(ns tipsy-shell.workspaces
  (:use [tipsy-shell.variables]
        [tipsy-shell.util]
        [tipsy-shell.http])
  (:require [clojure.data.json :as j])
  (:import [java.awt Desktop]
           [com.yahoo.tipsy.shell Utility]
           [com.yahoo.tipsy.shell.data WorkspacesData WorkspaceData]))

(defn read-workspaces []
  "Returns all the workspaces keys as a chimp string representation
Note. A compact form is not returned here since the chimp
representation is itself very terse"
  (GET (str "/ace/v1/workspaces") {:type :workspace}))

(defn read-workspace [key]
  "Returns as string the compact representation of the workspace having
  'workspace' attribute 'key'"
  (GET (str "/ace/v1/workspace/" key) {:type :workspace}))

(defn- add-fields [content key]
  "Adds key to content, also adds all
fields which can be auto-generated"
  (reduce (fn [content [path func]]
            (update-in content path func))
          content
          {[:workspace] (constantly key)
           [:grid_user] (constantly (cur-user))
           [:queue]     (constantly (cur-queue))
           [:email]     (constantly (str (cur-user) "@" (email-domain)))
           [:_rev]      rev
           [:_writer]   writer}))

(defn create-workspace
  [key & {:keys [fresh] :or {fresh false}}]
  "Allows user to create/modify a workspace.  'key' is the canonical
key of a workspace.  Opens up an editor to edit a workspace. The the
'workpsace' attribute in the json template will be filled with the
'key' specified."
  (let [content (-> (template key :workspace fresh)
                    slurp
                    j/read-json
                    (add-fields key)
                    indent-json)
        file (expected-template key :workspace)]
    (spit file content)
    (.edit (Desktop/getDesktop) file)))

(defn put-workspace [key]
  "Makes a put request for a workspace. Returns a http status map"
  (let [file (expected-template key :workspace)]
    (if (.exists file)
      (PUT (str "/ace/v1/workspace/" key)
           (slurp file)
           {:type :workspace
            :content-type "application/x-data+json"})
      (str "Workspace " key " not created. Please create the workspace first"))))
