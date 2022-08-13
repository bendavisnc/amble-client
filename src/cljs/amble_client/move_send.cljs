(ns amble-client.move-send
  "Listens to moves to send off to the server."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def move-resource-add!-chan (async/chan))
(def move-chan (async/chan))

(go-loop [move-resource-add! (async/<! move-resource-add!-chan)]
  (let [move-to-send (async/<! move-chan)]
    (println "Sending move to server.")
    (move-resource-add! (name (:game-id move-to-send))
                        (name (:player-id move-to-send))
                        (:player-piece-index move-to-send)
                        (:move move-to-send)
                        (:x move-to-send) 
                        (:y move-to-send) 
                        (:client-id move-to-send)))

  (recur move-resource-add!))

(defmethod ig/init-key :amble/move-send [_ {:keys [move-local-chan, move-resource-add!]}]
  (async/pipe move-local-chan amble-client.move-send/move-chan)
  (async/put! move-resource-add!-chan move-resource-add!)
  nil)


