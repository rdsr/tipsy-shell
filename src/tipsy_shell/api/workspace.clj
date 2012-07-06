(ns tipsy-shell.api.workspace
  (:use [tipsy-shell.variables]
        [tipsy-shell.ace]
        [tipsy-shell.util]
        [tipsy-shell.http])
  (:require [clojure.data.json :as j])
  (:import [java.awt Desktop]))

(defn read-workspaces
  "Returns all the workspaces keys as a
chimp string representation.
> (read-workspaces)"
  []
  (p-print (GET "/ace/v1/workspaces")))

(defn read-workspace
  "Returns as string the compact representation of the workspace having
  'workspace' attribute 'key'
> (read-workspace :tipsy.ws)"
  [key]
  (p-print (GET (str "/ace/v1/workspace/" (name key)))))

(def ^:private field-mappings
  {[:grid_user] (read-var :cur-user)
   [:queue]     "default"
   [:email]     (str (read-var :cur-user) "@" (read-var :email-domain))
   [:_rev]      (rev)
   [:_writer]   (writer)})

(defn edit-workspace
  "Allows user to edit a workspace by firing
up an editor. 'key' is the canonical key of
a workspace.
> (edit-workspace :tipsy.ws)"
  [key & [fresh]]
  (let [key (name key)
        content (slurp (template key :workspace fresh))
        file (expected-file key :workspace)]
    (spit file content)
    (.edit (Desktop/getDesktop) file)))

(defn put-workspace
  "Makes a put request for a workspace. Returns a http status map
> (put-workspace :tipsy.ws)"
  [key]
  (let [key (name key)
        file (expected-file key :workspace)
        compact (-> file
                    slurp
                    j/read-json
                    (assoc :workspace key) ;; canonical key
                    (add-defaults field-mappings))
        chimp (as-chimp :workspace)]
    (p-print
     (PUT (str "/ace/v1/workspace/" key)
          chimp
          {:content-type "application/x-data+json"}))))
