(ns tipsy-shell.util
  (:use [tipsy-shell.variables]
        [clojure.test])
  (:require [clojure.string :as s])
  (:import [java.io File]))

(deftest template-fns
  (is (= (-> (fresh-template :workspace :ac1.w1) .getAbsolutePath)
         (s/join File/separator [(compact-defs) "templates" "workspace" "ac1.w1.json"])))

  (is (= (-> (template :workspace :ac1.w1 false) .getAbsolutePath)
         (s/join File/separator [(compact-defs) "templates" "workspace" "ac1.w1.json"])))

  (let [path (s/join File/separator [(compact-defs) (cur-account) "workspace" "ac1.w2.json"])]
    (-> path File. .getParentFile .mkdirs)
    (-> path File. .createNewFile)
    (is (= (-> (template :workspace :ac1.w2 true) .getAbsolutePath)
           (s/join File/separator [(compact-defs) "templates" "workspace" "ac1.w2.json"])))
    (is (= (-> (template :workspace :ac1.w2 false) .getAbsolutePath)
           (s/join File/separator [(compact-defs) (cur-account) "workspace" "ac1.w2.json"])))))

(deftest compact-chimp-fns
  (is (= (uri->type "/ace/v1/workspace/ac1.ws1") :workspace))
  (is (= (uri->type "/ace/v1/executions/ac1.ws1.t1") :executions)))

(defn setup [f]
  "Setup variables"
  (doseq [[k v] [[:compact-defs "/tmp/tipsy/compact_defs"] [:cur-account "rdsr"]]]
    (variables/update k v))
  (f))

(use-fixtures :once setup)
(run-tests)