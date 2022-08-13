(ns amble-client.move-local
  "Handles moves made locally.
   This segment is only responsible with making sure a move finds its place once the local move is over.
   If we didn't care about waiting for the server, then things would be a bit different and none of this would exist."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.move-helpers :refer [end-move-at-point!]])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))

(def move-chan (async/chan))

(go-loop [app-atom (async/<! app-atom-chan)]
  (let [move-local (async/<! move-chan)]
    (println "Handling local move.")
    (end-move-at-point! app-atom move-local)
    (recur app-atom)))

(defmethod ig/init-key :amble/move-local [_ {:keys [app-atom, app-ready-chan, move-local-chan]}]
  (async/pipe move-local-chan amble-client.move-local/move-chan)
  (go
    (async/<! app-ready-chan)
    (async/>! app-atom-chan app-atom))
  nil)


