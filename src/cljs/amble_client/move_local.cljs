(ns amble-client.move-local
  "Handles moves made locally."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.environment :refer [environment]]
            [amble-client.interpolate-function :refer [interpolate-function]]
            [haslett.client :as haslett-client])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))

(def move-chan (async/chan))

(go-loop [app-atom (async/<! app-atom-chan)]
  (let [{:keys [player-id, player-piece-index, x, y, move]} (async/<! move-chan)
        [last-x, last-y] (last move)]
    (println "Handling local move.")
    (interpolate-function (fn [{:keys [x, y]}]
                            (swap! app-atom assoc-in [:player-pieces player-id player-piece-index] [x, y]))
                          :x1 last-x
                          :y1 last-y
                          :x2 x
                          :y2 y)
                     
    ;; (println move)
    (recur app-atom)))

(defmethod ig/init-key :amble/move-local [_ {:keys [app-atom, app-ready-chan, move-local-chan]}]
  (async/pipe move-local-chan amble-client.move-local/move-chan)
  (go
    (async/<! app-ready-chan)
    (async/>! app-atom-chan app-atom))
  nil)


