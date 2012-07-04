(ns tipsy-shell.execution
  (:use [tipsy-shell.http]))

(defn read-triggers
  "Lists the dependent triggers for all channels
of the task. That is, for each channel, a list of
dependent other tasks that it triggers are enumerated.
This information is dynamically calculated by the ACE
instance (not part of the config). This information
can be used to build a dependency graph of execution."
  [task-key]
  (GET (str "/ace/v1/triggers/" (name task-key))))

(defn read-executions
  "Return an enumeration of all executions
of this task. Each execution id is the revision
assigned when the execution occurred."
  [task-key & [start count]]
  (GET (str "/ace/v1/executions/" (name task-key)) {:start start count :count}))

(defn read-execution
  "Return the data for a particular execution
(a full key that includes the task key as its
namespace)."
  [exec-key]
  (GET (str "/ace/v1/execution/" exec-key)))

(defn post-executions
  "Execute a task manually, with the given
mode (full, incr, or replay_latest)."
  [task-key]
  (throw (UnsupportedOperationException. "Not currently supported.")))