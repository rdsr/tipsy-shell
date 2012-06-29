(ns tipsy-shell.variables
  (:import [java.io File]))

(defn compact-defs-dir []
  (let [dir (str (System/getProperty "user.home") "/.tipsy/compact_defs")]
    (.mkdirs (File. dir))
    dir))

(def default-vars
  {:bouncer-cookie ""
   :bouncer-url    ""
   :cur-account    ""
   :cur-grid-user  (System/getProperty "user.name")
   :cur-queue      "default"
   ;:cur-task       ""
   :cur-user       (System/getProperty "user.name")
   :cur-dir        (System/getProperty "user.dir")
   ;:cur-workspace  ""
   :email-domain   "yahoo-inc.com"
   ;:display-chimp  false
   :base-url       "http://localhost:4080"
   :version       "v1"
   :compact-defs   (compact-defs-dir)})


(def ^:private var->val (atom default-vars))

(defn read-var [key]
  (@var->val key (str (keyword key) "Not defined")))

(defn update-var [key value]
  (swap! var->val (fn [m] (assoc m (keyword key) value)))
  'done!)

(defn all-vars [] @var->val)

(defn update-vars [& kvs]
  (doseq [[k v] kvs]
    (update-var k v))
  'done!)

;; (defmacro create-fn [variable]
;;   `(defn ~(-> variable name symbol)
;;      []
;;      (~variable @var->val)))

;; ;; TODO: make it a loop
;; (create-fn :bouncer-cookie)
;; (create-fn :bouncer-url)
;; (create-fn :cur-account)
;; (create-fn :cur-grid-user)
;; (create-fn :cur-queue)
;; (create-fn :cur-task)
;; (create-fn :cur-user)
;; (create-fn :cur-dir)
;; (create-fn :cur-workspace)
;; (create-fn :email-domain)
;; (create-fn :display-chimp)
;; (create-fn :base-url)
;; (create-fn :version)
;; (create-fn :compact-defs)

;; TODO: create fns to update also