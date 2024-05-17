(ns amble-client.move-remote
  "Handles moves made remotely"
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [goog.string :as gstring]
            [goog.string.format]
            [amble-client.move-helpers :refer [end-move-at-point!]]
            [amble-client.move-replay :refer [replay-move!]])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))
(def move-chan (async/chan))

(go-loop [app-atom (async/<! app-atom-chan)]
  (let [{:keys [player-id, player-piece-index] :as move-remote} (async/<! move-chan)
        player-id (keyword player-id)
        move-remote (assoc move-remote :player-id player-id)]
    (println "Handling remote move.")
    (println move-remote)
    (if (:is-remote? move-remote)
      (do (println "Playing move made remotely.")
          (replay-move! (fn [{:keys [x, y]}]
                          (swap! app-atom assoc-in [:player-pieces player-id (js/parseInt player-piece-index)] [x, y]))
                        move-remote
                        (fn []
                          (end-move-at-point! app-atom move-remote))))
      ;; else
      (println (gstring/format "Ignoring received remote move, as it was made locally, `%s`."
                               (:id move-remote))))
    (recur app-atom)))

(defmethod ig/init-key :amble/move-remote [_ {:keys [app-atom, app-ready-chan, move-remote-chan]}]
  (async/pipe move-remote-chan amble-client.move-remote/move-chan)
  (go
    (async/<! app-ready-chan)
    (async/>! app-atom-chan app-atom))
  nil)


