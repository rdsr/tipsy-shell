(ns tipsy-shell.api.task.zip
  (:use [tipsy-shell.variables :only (read-var)]
        [tipsy-shell.util :only (fix-path)])
  (:require [clojure.java.io :as io])
  (:import [java.io File FileOutputStream BufferedOutputStream]
           [java.util.zip ZipEntry ZipOutputStream]))

;; zip utils for executable task

(defn- zipfile-from
  "Given a task key, construct a new zip file."
  [task-key]
  (File. (str (or (read-var :zip-dir) "/tmp")
              "/"
              task-key
              ".zip")))

(defn- create-resources
  "Given a dir, reads all the files under
it and it's children dirs and returns
a {'pathof file relative to dir' -> 'abs file path'} map."
  [dir]
  (let [dir-sz (count (.getAbsolutePath dir))]
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

(defn zip-from-pig
  "Create a zip file given a task key,
a pig script path and lib dir. The
contents of the chimp structure are
also added at the top level of the
zip structure. Here chimp is expected
to be in string format."
  [task-key script lib chimp]
  (let [script (-> script fix-path File.)
        lib (-> lib fix-path File.)]
    (create-zip (zipfile-from task-key)
                (merge (create-resources lib)
                       {(str "resources/" (.getName script)) script
                        "taskdef.json" chimp}))))
