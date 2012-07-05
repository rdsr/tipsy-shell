(ns tipsy-shell.api.execution
  (:use [tipsy-shell.http]
        [tipsy-shell.util :only (p-print)]))

(defn read-triggers
  "Lists the dependent triggers for all channels
of the task. That is, for each channel, a list of
dependent other tasks that it triggers are
enumerated.
> (read-triggers :tipsy.ws.it)"
  [task-key]
  (p-print (GET (str "/ace/v1/triggers/" (name task-key)))))

(defn read-executions
  "Return an enumeration of all executions
of this task. Each execution id is the revision
assigned when the execution occurred.
> (read-executions :tipsy.ws.it)"
  [task-key & [start count]]
  (p-print (GET (str "/ace/v1/executions/" (name task-key))
                {:query-params {:start start :count count}})))

(defn read-execution
  "Return the data for a particular execution
 (a full key that includes the task key as its
namespace).
> (read-execution :tipsy.ws.20110805-141149-023)"
  [exec-key]
  (p-print (GET (str "/ace/v1/execution/" (name exec-key)))))

(defn post-executions
  "Execute a task manually, with the given
mode (full, incr, or replay_latest)."
  [task-key]
  (throw (UnsupportedOperationException. "Not currently supported.")))