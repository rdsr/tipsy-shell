(ns tipsy-shell.core
  (:require [clojure.repl :as r]
            [tipsy-shell.variables :as v]
            [tipsy-shell.workspace :as w]
            [tipsy-shell.task :as t]
            [tipsy-shell.data.workspace]
            [tipsy-shell.data.importer-task]
            [tipsy-shell.data.executable-task]
            [tipsy-shell.channel :as c]
            [tipsy-shell.util :as u]
            [clojure.java.shell :as s]))



;; Making  all names explicit here since lrwrap will need
;; to read only one file 'core.clj and other fns/vars etc
;; won't be exposed to user when auto-completing.'

(refer 'clojure.repl :only '[doc])
(refer 'tipsy-shell.variables :only '[read-var update-var update-vars])
(refer 'tipsy-shell.channel :only '[put-channel post-channel])

(defn- with-indent [f & args]
  (println (u/indent (apply f args)))
  'done!)

(defn read-workspaces
  {:doc (-> w/read-workspaces var meta :doc)}
  []
  (with-indent w/read-workspaces))

(defn read-workspace
  {:doc (-> w/read-workspaces var meta :doc)}
  [key]
  (with-indent w/read-workspace key))

(defn read-tasks [ws-key]
  {:doc (-> t/read-tasks var meta :doc)}
  [key]
  (with-indent t/read-tasks ws-key))

(defn read-task [key]
  {:doc (-> t/read-task var meta :doc)}
  [key]
  (with-indent t/read-task key))

(defn all-vars []
  "Returns all vars with their corresponding
values.

Example
> (all-vars)"
  (doseq [[k v] (v/all-vars)]
    (println k "->" v))
  'done!)

(defn- standard-doc []
  (println "An editor should pop up just about
now. Make necessary changes to the template
and save the file. The template also serves as
documentation on how to edit it. The content of
the file is json. You'd only need to edit some
values of the json object. As explained below.

Values having:

'##' You need to edit these values.
'#O' These are optional values.
'#D' Specifies what is the default value for the
optional value.

A special '_comment' attribute is added to serve
as further documentation. This is not part of this
compact definition.

Press enter when done!"))

(defn create-workspace
  "Create and upload a workspace definition

Example
> (create-workspace \"tipsy.ws\")
> (create-workspace :tipsy.ws)  ;; same as before
> (create-workspace \"tipsy.ws\" :fresh)"
  [key & [fresh]]
  (w/edit-workspace key fresh)
  (standard-doc)
  (read-line)
  (w/put-workspace key))


(defn create-importer-task
  "Creates and uploads an importer task definition

Example
> (create-importer-task :tipsy.ws.it)
> (create-importer-task \"tipsy.ws.it\")
> (create-importer-task \"tipsy.ws.it\" :fresh)"
  [key & [fresh]]
  (t/edit-importer-task key fresh)
  (standard-doc)
  (read-line)
  (t/put-importer-task key))


(defn create-executable-task
  "Creates and uploads an importer task definition

Example
> (create-executable-task :tipsy.ws.et :pig)
> (create-executable-task \"tipsy.ws.et\" :pig)
> (create-executable-task \"tipsy.ws.et\" :pig :fresh)"
  [key exec & [fresh]]
  (t/edit-executable-task key exec fresh)
  (standard-doc)
  (read-line)
  (t/put-executable-task key exec))

(defmacro sh [& args]
  "Executes a system command

Example
> (sh ls -ltr /)
> (sh pwd)"
  (let [args (map name args)
        {:keys [exit out err]} (apply s/sh args)]
    (if (= exit 0) (println out) (println err))
    :done!))

(println "
Type (doc symbol-name) where symbol-name may be one of:

all-vars
update-var
read-workspace
create-workspace
read-task
sh

or most of what you see when you press tab.
")
