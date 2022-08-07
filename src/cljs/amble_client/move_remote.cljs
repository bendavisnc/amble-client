(ns amble-client.move-remote
  "Handles moves made remotely"
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.environment :refer [environment]]
            [haslett.client :as haslett-client])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))

(def move-chan (async/chan))

(go-loop [app-atom (async/<! app-atom-chan)]
  (let [{:keys [player-id, player-piece-index, x, y] :as move} (async/<! move-chan)]
    (println "Handling remote move. - todo")
    (println move)
    (recur app-atom)))

(defmethod ig/init-key :amble/move-remote [_ {:keys [app-atom, move-remote-chan]}]
  (async/pipe move-remote-chan amble-client.move-remote/move-chan)
  (js/setTimeout (fn [& args]
                   (async/put! app-atom-chan app-atom))
                 1000) 
  nil)


