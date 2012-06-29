(ns tipsy-shell.task
  (:use [tipsy-shell.util]
        [tipsy-shell.http]
        [tipsy-shell.variables :only (read-var)]
        [tipsy-shell.data :only (rev writer)])
  (:require [clojure.data.json :as j]
            [clojure.java.io :as io])
  (:import [java.io File BufferedOutputStream]
           [java.util.zip ZipEntry ZipOutputStream]
           [java.awt Desktop]))

(defn read-tasks [ws-key]
  "Given a canonical workspace key, enumerates
all tasks in the specified workspace."
  (GET (str "/ace/v1/tasks/" ws-key) {:type :tasks}))

(defn read-task [key & [snapshot]]
  "Given a canonical task key, gets the
representation for a deployed Task. The snapshot
parameter is optional, and defaults to the most
recently deployed revision. If present, the most
recent deployment that is within the snapshot
range is returned."
  (GET (str "/ace/v1/task/" key)
       (merge {:type :task}
              (if snapshot {:snapshot snapshot} {}))))

(defn- create-task [key type fresh]
  (let [content (slurp (template key type fresh))
        file (expected-template key :task)]
    (spit file content)
    (.edit (Desktop/getDesktop) file)))

(defn create-importer-task
  [key & [fresh]]
  "Given a canonical task key, pops up a
template of an importer task for editing.
The template may be fresh or previously
edited depending upon the 'fresh' arg."
  (create-task key :importer-task fresh))

(defn- executable-type [exec]
  (keyword (str "executable-" (name exec) "-task")))

(defn create-executable-task
  [key exec & [fresh]]
  "Given a canonical task key, pops up a
template of an executable task for editing.
The template may be fresh or previously
edited depending upon the 'fresh' arg. The
exec arg. determines the type of task
PIG|OOZIE"
  (create-task key (executable-type exec) fresh))

(def ^:private auto-gen-fields
  {[:task]    (partial add-field key)
   [:_rev]    (partial add-field (rev))
   [:_writer] (partial add-field (writer))})

(defn put-importer-task [key]
  "Given a canonical task key, makes a put
request for an importer task. Returns a http
status map as response."
  (if-let [file (expected-template key :importer-task)]
    (let [content (-> file
                      slurp
                      j/read-json
                      (add-fields key auto-gen-fields)
                      ;; after adding defaults, remove the ones which are still thr.
                      remove-defaults)]
      (PUT (str "/ace/v1/task/" key)
           content
           {:type :importer-task
            :content-type "application/x-data+json"}))
    (str "Task " key " not created. Please create the task first")))

(defn- add-entry [name resource os]
  (let [entry (ZipEntry. name)]
    (.putNextEntry os entry)
    (io/copy resource os)))

(defn- create-zip [content]
  "Currently just works for pig executable."
  (let [f (if (read-var :zip-dir)
            (File. (read-var :zip-dir))
            (File/createTempFile "tipsy" "zip" "/tmp"))
        script  (get content :script)
        lib-dir (get content :lib_dir)]
    (with-open [os (-> f BufferedOutputStream. ZipOutputStream.)]
      (add-entry "taskdef.json" (j/json-str content) os)
      (doseq [resource (file-seq (File. lib-dir))]
        (let [name (.getName resource)]
          (add-entry (str "resource/" name) resource os))))))

(defn put-executable-task [key exec]
  "Given a canonical task key, makes a put
request for an executable task. This constructs
a zip by including all resources, reading them
from taskdef json. Returns a http status map as
response. The exec arg. determines the type
PIG|OOZIE task to upload."
  (if-let [file (expected-template key (executable-type exec))]
    (let [content (-> file
                      slurp
                      j/read-json
                      (add-fields key auto-gen-fields)
                      ;; after adding defaults, remove the ones which are still thr.
                      remove-defaults)]
      (PUT (str "/ace/v1/task/" key)
           (create-zip content)
           {:type (executable-type exec)
            :content-type "application/zip"}))
    (str "Task " key " not created. Please create the task first")))
