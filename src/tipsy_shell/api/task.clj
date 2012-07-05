(ns tipsy-shell.api.task
  (:use [tipsy-shell.api.task.zip]
        [tipsy-shell.util]
        [tipsy-shell.http]
        [tipsy-shell.ace]
        [tipsy-shell.ace.importer-task]
        [tipsy-shell.ace.executable-task])
  (:require [clojure.data.json :as j]
            [clojure.java.io :as io])
  (:import [java.io File FileOutputStream BufferedOutputStream]
           [java.util.zip ZipEntry ZipOutputStream]
           [java.awt Desktop]))

(defn read-tasks
  "Given a canonical workspace key, enumerates
all tasks in the specified workspace.
> (read-tasks :tipsy.ws)"
  [ws-key]
  (p-print (GET (str "/ace/v1/tasks/" (name ws-key)))))

(defn read-task
  "Given a canonical task key, gets the
representation for a deployed Task. The snapshot
parameter is optional, and defaults to the most
recently deployed revision. If present, the most
recent deployment that is within the snapshot
range is returned.
> (read-task :tipsy.ws.it)"
  [key & [snapshot]]
  (p-print (GET (str "/ace/v1/task/" (name key))
                {:query-params {:snapshot snapshot}})))

(defn- edit-task [key type fresh]
  (let [content (slurp (template key type fresh))
        file (expected-file key type)]
    (spit file content)
    (.edit (Desktop/getDesktop) file)))

(defn edit-importer-task
  "Given a canonical task key, pops up a
template of an importer task for editing.
The template may be fresh or previously
edited depending upon the 'fresh' arg.
> (edit-importer-task :tipsy.ws.it)
> (edit-importer-task :tipsy.ws.it :fresh)"
  [key & [fresh]]
  (edit-task key :importer-task fresh))

(defn- executable-type [exec]
  (keyword (str "executable-" (name exec) "-task")))

(defn edit-executable-task
  "Given a canonical task key, pops up a
template of an executable task for editing.
The template may be fresh or previously
edited depending upon the 'fresh' arg. The
exec arg. determines the type of task :pig | :oozie
> (edit-importer-task :tipsy.ws.et :pig)
> (edit-importer-task :tipsy.ws.et :pig :fresh)"
  [key exec & [fresh]]
  (edit-task key (executable-type exec) fresh))

(def ^:private field-mappings
  "A map of paths and fns. The corresponding
functions generate the field at the given paths.
A :i in the path implies that there would be
mutiple children having a path just before :i,
so same fn has to be applied to all of them.
Currently this same map is used for both
importer and executable tasks. Hope it all
works out."
  {[:_rev] (rev)
   [:_writer] (writer)
   [:lib_dir] "lib"}) ;; adding in shell since this default value will be used to construct a zip

(defn put-importer-task
  "Given a canonical task key, makes a put
request for an importer task. It adds the
necessary default feilds and converts data
to chimp before uploading.
> (put-importer-task :ws.tipsy.it)"
  [key]
  (let [key (name key)
        file (expected-file key :importer-task)
        content (-> file
                    slurp
                    j/read-json
                    (assoc :task key)
                    (add-defaults field-mappings)
                    (as-chimp :importer-task))]
    (p-print
     (PUT (str "/ace/v1/task/" (name key))
          content
          {:content-type "application/x-data+json"}))))

(defn put-executable-task
  "Given a canonical task key, makes a put
request for an executable task. This constructs
a zip by including all resources, reading them
from :lib_dir attribute present in compact def.
and also from the script attribute. The type
arg. determines the type of executable task
:pig | :oozie"
  [key exec]
  (let [key (name key)
        type (executable-type exec)
        file (expected-file key type)
        compact (-> file
                    slurp
                    j/read-json
                    (assoc :task key)
                    (add-defaults field-mappings))
        chimp (as-chimp compact type)
        zip (zip-from-pig key
                          (:script compact)
                          (:lib_dir compact)
                          chimp)]
    (p-print
     (PUT (str "/ace/v1/task/" (name key))
          zip
          {:content-type "application/zip"}))))
