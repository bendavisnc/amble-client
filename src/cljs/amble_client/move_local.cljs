(ns amble-client.move-local
  "Handles moves made locally."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.environment :refer [environment]]
            [amble-client.interpolate-function :refer [interpolate-function]]
            [amble-client.move-helpers :refer [end-move-at-point!]]
            [haslett.client :as haslett-client])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))

(def move-chan (async/chan))

(go-loop [app-atom (async/<! app-atom-chan)]
  (let [{:keys [player-id, player-piece-index, x, y, move] :as move-local} (async/<! move-chan)
        [last-x, last-y] (last move)]
    (println "Handling local move.")
    (end-move-at-point! app-atom move-local)
    ;; (println move)
    (recur app-atom)))

(defmethod ig/init-key :amble/move-local [_ {:keys [app-atom, app-ready-chan, move-local-chan]}]
  (async/pipe move-local-chan amble-client.move-local/move-chan)
  (go
    (async/<! app-ready-chan)
    (async/>! app-atom-chan app-atom))
  nil)


