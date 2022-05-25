(ns amble-client.move-update-event-handler
  "Handles subscribing and reacting to remote move events."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))


(def latest-move-chan (async/chan))

(go-loop [resource (async/<! resource-chan)]
  (let [[game-id, move-index] (async/<! latest-move-chan)
        latest-move (resource/get game-id, move-index)]

    (println "hey this is from remote") 
    (println latest-move)
    (recur resource)))
   

(defn on-event! [game-id, move-index]
  (async/put! lastest-move-chan [game-id, move-index]))

(defmethod ig/init-key :amble/move-update-event-handler [_ {:keys [register-event-handler!]}]
  (let [event-handler
        {:on-event! on-event!}] 
    (register-event-handler! event-handler)
    event-handler))


