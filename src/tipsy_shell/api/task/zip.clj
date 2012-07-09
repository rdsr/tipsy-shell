(ns tipsy-shell.api.task.zip
  (:use [tipsy-shell.variables :only (read-var)]
        [tipsy-shell.util :only (fix-path)])
  (:require [clojure.java.io :as io])
  (:import [java.io File FileOutputStream BufferedOutputStream]
           [java.util.zip ZipEntry ZipOutputStream]))

;; zip utils for executable task

(defn- zip-filepath
  "Given a task key, construct a new zip file."
  [key]
  (File. (str (or (read-var :zip-dir) "/tmp")
              "/"
              (name key)
              ".zip")))

(defn- create-resources
  "Given a dir, reads all the files under
it and it's children dirs and returns
a {'pathof file relative to dir' -> 'abs file path'} map."
  [dir]
  (let [dir (if (string? dir) (File. dir) dir)
        dir-sz (count (.getAbsolutePath dir))]
    (reduce (fn [m file]
              (assoc m
                (str "resources"
                     (subs (.getAbsolutePath file) dir-sz))
                file))
            {}
            (filter #(.isFile %) (file-seq dir)))))

(defn- add-entry [name resource os]
  "Given a resource (File or content as string)
adds a zip entry to an zip output stream."
  (let [entry (ZipEntry. name)]
    (.putNextEntry os entry)
    (io/copy resource os)
    (.closeEntry os)))

(defn- create-zip
  "Given a file and a resources map,
tries to create a zip file having path
'filepath'. The resources map should
have a key which corresponds to the
'name' in ZipEntry and the value should
be the actual 'File' or string and should
correspond to a 'resource' in ZipEntry.
Returns the zip file created."
  [file resources]
  (with-open [os (-> file FileOutputStream. BufferedOutputStream. ZipOutputStream.)]
    (doseq [[path resource] resources]
      (add-entry path resource os)))
  file)

(defn- zip-from-internal
  "See zip-from"
  [key chimp script-wf lib]
  (let [script-wf (-> script-wf fix-path File.)
        lib (fix-path lib)]
    (create-zip (zip-filepath key)
                (merge (create-resources lib)
                       {(str "resources/" (.getName script-wf)) script-wf
                        "taskdef.json" chimp}))))

(defmulti zip-from
  "Creates a zip from either a pig or
oozie executable task. Everything under
lib_dir is added under a toplevel
'resources' folder in the zip. The contents
of the chimp structure are added at the top
level in a 'taskdef.json' file.  Also,
either a pig script or workflow.xml is
added under resources in zip."
  (fn [_ _ _ exec] exec))

(defmethod zip-from
  :pig
  [key compact chimp _]
  (let [{:keys [script lib_dir]} compact]
    (zip-from-internal key chimp script lib_dir)))

(defmethod zip-from
  :oozie
  [key compact chimp _]
  (let [{:keys [workflow_path lib_dir]} compact]
    (zip-from-internal key chimp workflow_path lib_dir)))
