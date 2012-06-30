(ns tipsy-shell.task
  (:use [tipsy-shell.util]
        [tipsy-shell.http]
        [tipsy-shell.variables :only (read-var)]
        [tipsy-shell.data]
        [tipsy-shell.data.importer-task]
        [tipsy-shell.data.executable-task])
  (:require [clojure.data.json :as j]
            [clojure.java.io :as io])
  (:import [java.io File FileOutputStream BufferedOutputStream]
           [java.util.zip ZipEntry ZipOutputStream]
           [java.awt Desktop]))

(defn read-tasks
  "Given a canonical workspace key, enumerates
all tasks in the specified workspace."
  [ws-key]
  (GET (str "/ace/v1/tasks/" (name ws-key)) {:type :tasks}))

(defn read-task
  "Given a canonical task key, gets the
representation for a deployed Task. The snapshot
parameter is optional, and defaults to the most
recently deployed revision. If present, the most
recent deployment that is within the snapshot
range is returned."
  [key & [snapshot]]
  (GET (str "/ace/v1/task/" (name key))
       (merge {:type :task}
              (if snapshot {:snapshot snapshot} {}))))

(defn- edit-task [key type fresh]
  (let [content (slurp (template key type fresh))
        file (expected-file key type)]
    (spit file content)
    (.edit (Desktop/getDesktop) file)))

(defn edit-importer-task
  "Given a canonical task key, pops up a
template of an importer task for editing.
The template may be fresh or previously
edited depending upon the 'fresh' arg."
  [key & [fresh]]
  (edit-task key :importer-task fresh))

(defn- executable-type [exec]
  (keyword (str "executable-" (name exec) "-task")))

(defn edit-executable-task
  "Given a canonical task key, pops up a
template of an executable task for editing.
The template may be fresh or previously
edited depending upon the 'fresh' arg. The
exec arg. determines the type of task :pig | :oozie"
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
   [:lib_dir] "lib" ;; adding in shell since this default value will be used to construct a zip
   })

(defn put-importer-task
  "Given a canonical task key, makes a put
request for an importer task. It adds the
necessary default feilds and converts data
to chimp before uploading."
  [key]
  (let [key (name key)
        file (expected-file key :importer-task)
        content (-> file
                    slurp
                    j/read-json
                    (assoc :task key)
                    (add-defaults field-mappings)
                    (as-chimp :importer-task))]
    (PUT (str "/ace/v1/task/" (name key)) content {:content-type "application/x-data+json"})))


;; zip utils for executable task
(defn- add-entry [name resource os]
  (let [entry (ZipEntry. name)]
    (.putNextEntry os entry)
    (io/copy resource os)
    (.closeEntry os)))

(defn- zip [file resources]
  (with-open [os (-> file FileOutputStream. BufferedOutputStream. ZipOutputStream.)]
    (doseq [[path resource] resources]
      (add-entry path resource os)))
  file)

(defmulti create-zip (fn [_ _ type] type))

(defn- zip-file [key]
  (File. (str (or (read-var :zip-dir) "/tmp") "/" key ".zip")))

(defmethod create-zip
  :executable-pig-task [compact chimp _]
  (let [script  (-> compact (get :script) fix-path File.)
        lib-dir (-> compact (get :lib_dir) fix-path File.)
        ;; add taskdef and script to resources
        resources {"taskdef.json" chimp (str "resources/" (.getName script)) script}]
    (zip (zip-file (get compact :task))
         (reduce
          ;; also add all under lib folder
          (fn [r f]
            (assoc r
              (str "resources" (subs (.getAbsolutePath f)
                                     (count (.getAbsolutePath lib-dir))))
              f))
          resources
          ;; log if file doesn't exit
          (filter (fn [f] (.isFile f)) (file-seq lib-dir))))))

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
        zip (create-zip compact chimp type)]
  (PUT (str "/ace/v1/task/" (name key)) zip {:content-type "application/zip"})))
