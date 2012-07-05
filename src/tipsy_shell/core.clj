(ns tipsy-shell.core
  (:require [clojure.java.shell :as s]
            [tipsy-shell.ace.executable-task]
            [tipsy-shell.ace.importer-task]
            [tipsy-shell.ace.workspace])
  (:use [clojure.repl]
        [tipsy-shell.api.task]
        [tipsy-shell.api.workspace]
        [tipsy-shell.api.channel]
        [tipsy-shell.api.execution]
        [tipsy-shell.variables]))

;; requiring all namespaces here, since this file will
;; be loaded by the repl.

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
  "Create and upload a workspace definition.
> (create-workspace :tipsy.ws)
> (create-workspace :tipsy.ws :fresh)"
  [key & [fresh]]
  (edit-workspace key fresh)
  (standard-doc)
  (read-line)
  (put-workspace key))

(defn create-importer-task
  "Creates and uploads an importer task definition
> (create-importer-task :tipsy.ws.it)
> (create-importer-task :tipsy.ws.it :fresh)"
  [key & [fresh]]
  (edit-importer-task key fresh)
  (standard-doc)
  (read-line)
  (put-importer-task key))

(defn create-executable-task
  "Creates and uploads an importer task definition.
> (create-executable-task :tipsy.ws.et :pig)
> (create-executable-task :tipsy.ws.et :pig :fresh)"
  [key exec & [fresh]]
  (edit-executable-task key exec fresh)
  (standard-doc)
  (read-line)
  (put-executable-task key exec))

(defn sh [cmd]
  "Executes a system command.
> (sh \"ls -ltr /\")
> (sh \"pwd\")"
  (let [args (.split cmd "\\s+")
        {:keys [exit out err]} (apply s/sh args)]
    (if (= exit 0) (println out) (println err))))

(println "
Type (doc symbol-name) where symbol-name may be one of:

all-vars
read-var
update-var
update-vars

read-workspaces
read-workspace
create-workspace

read-tasks
read-task
create-importer-task
create-executable-task

post-channel
put-channel
read-channels
etc..

sh

Or most of what you see when you press tab.")
