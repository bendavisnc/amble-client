(ns amble-client.move-update-event-handler
  "Handles subscribing and reacting to remote move events."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

;; Used for the initial notification that there's a new move.
(def latest-move-chan (async/chan))

;; Used to call up when a successful, remote move is ready.
(def on-move!-chan (async/chan))

;; Holds a function that makes a server call for a get move request.
(def resource-chan (async/chan))

(go-loop [
          on-move! (async/<! on-move!-chan)
          resource (async/<! resource-chan)]
  (let [[game-id, move-index] (async/<! latest-move-chan)
        latest-move (resource/get game-id, move-index)]

    (println "hey this is from remote") 
    (println latest-move)
    (recur on-move! resource)))
   

(defn on-event! [game-id, move-index]
  (async/put! lastest-move-chan [game-id, move-index]))

(defmethod ig/init-key :amble/move-update-event-handler [_ {:keys [on-move!, resource, register-event-handler!]}]
  (let [event-handler
        {:on-event! on-event!}] 
    (async/put! resource-chan resource)   
    (async/put! on-move!-chan on-move!)   
    (register-event-handler! event-handler)
    event-handler))



