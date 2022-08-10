(ns amble-client.move-remote
  "Handles moves made remotely"
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.environment :refer [environment]]
            [amble-client.move-helpers :refer [end-move-at-point!]]
            [amble-client.move-replay :refer [replay-move!]]
            [haslett.client :as haslett-client])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))

(def move-chan (async/chan))

(go-loop [app-atom (async/<! app-atom-chan)]
  (let [{:keys [player-id, player-piece-index, x, y, move] :as move-remote} (async/<! move-chan)
        player-id (keyword player-id)
        move-remote (assoc move-remote :player-id player-id)]
    (println "Handling remote move.")
    (replay-move! (fn [{:keys [x, y]}]
                    (swap! app-atom assoc-in [:player-pieces player-id (js/parseInt player-piece-index)] [x, y]))
                  move-remote
                  (fn []
                    (end-move-at-point! app-atom move-remote)))
    ;; (println move)
    (recur app-atom)))


(defmethod ig/init-key :amble/move-remote [_ {:keys [app-atom, app-ready-chan, move-remote-chan]}]
  (async/pipe move-remote-chan amble-client.move-remote/move-chan)
  (go
    (async/<! app-ready-chan)
    (async/>! app-atom-chan app-atom))
  nil)


