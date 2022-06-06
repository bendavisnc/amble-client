(ns amble-client.move-send
  "Listens to moves to send off to the server."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def move-resource-add!-chan (async/chan))
(def move-chan-chan (async/chan))

(go-loop [move-resource-add! (async/<! move-resource-add!-chan)
          move-chan (async/<! move-chan-chan)]
  (let [move-to-send (async/<! move-chan)]
    (println "Sending move to server.")
    (move-resource-add! (name (:game-id move-to-send))
                        (name (:player-id move-to-send))
                        (:player-piece-index move-to-send)
                        (:move move-to-send)))
  (recur move-resource-add!
         move-chan))

(defmethod ig/init-key :amble/move-send [_ {:keys [move-chan, move-resource-add!]}]
  (async/put! move-chan-chan move-chan)
  (async/put! move-resource-add!-chan move-resource-add!)
  nil)


