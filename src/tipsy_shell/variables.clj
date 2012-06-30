(ns tipsy-shell.variables
  (:import [java.io File]))

(defn- compact-defs-dir []
  (let [dir (str (System/getProperty "user.home") "/.tipsy/compact_defs")]
    (.mkdirs (File. dir))
    dir))

(def ^:private default-vars
  {:bouncer-cookie ""
   :bouncer-url    ""
   :cur-account    ""
   :cur-grid-user  (System/getProperty "user.name")
   :cur-user       (System/getProperty "user.name")
   :cur-dir        (System/getProperty "user.dir")
   :email-domain   "yahoo-inc.com"
   :base-url       "http://localhost:4080"
   :version       "v1"
   :compact-defs   (compact-defs-dir)})

(def ^:private var->val (atom default-vars))

(defn read-var
  "Example
> (read-var :cur-user)"
  [key] (@var->val (keyword key)))

(defn update-var
  "Example
> (update-var :cur-user \"rdsr\")"
  [key value]
  (swap! var->val (fn [m] (assoc m (keyword key) value)))
  'done!)

(defn all-vars
  "Returns all vars with their corresponding
values"
  [] @var->val)

(defn update-vars
  "Updates multiple vars simultaneously

Example
> (update-vars :cur-user \"rdsr\" :cur-dir \"/tmp/tipsy\")"
  [& kvs]
  (doseq [[k v] (apply hash-map kvs)]
    (update-var k v)) 'done!)
