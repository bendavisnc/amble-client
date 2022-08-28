(ns amble-client.move-record-check
  "Keeps a record of local moves made, so they don't get replayed later, asynchronously."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def move-chan (async/chan))

(def client-ids (atom {}))

(defn move-record-check
  "Returns if this move is recorded as made, according to the list of client ids."
  [client-id]
  ((deref client-ids)
   client-id))

(go-loop []
  (let [move (async/<! move-chan)]
    (println (str "Recording new move, " (:client-id move) "."))
    (swap! client-ids assoc (:client-id move) true))
  (recur))

(defmethod ig/init-key :amble/move-record-check [_ {:keys [move-local-chan]}]
  (async/pipe move-local-chan amble-client.move-record-check/move-chan)
  move-record-check)


