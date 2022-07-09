(ns amble-client.move-receive
  "Listens to moves from server."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def latest-move-index-chan (async/chan))
(def move-resource-get!-chan (async/chan))
(def app-atom-chan (async/chan))
(def move-chan (async/chan))

(go-loop [move-resource-get! (async/<! move-resource-get!-chan)
          app-atom (async/<! app-atom-chan)
          game-id (:game-id (deref app-atom))]
  (let [latest-move-index (async/<! latest-move-index-chan)
        _ (println (str "New move announced from server, index " latest-move-index "."))
        latest-move-from-server (async/<! (move-resource-get! game-id, latest-move-index))
        latest-move (assoc latest-move-from-server :origin :remote)]
    (println "Requested latest move.")
    (assert (not (empty? (:move latest-move)))
            "Received invalid move!") 
    (async/>! move-chan latest-move)
    (recur move-resource-get!, app-atom, game-id)))

(defmethod ig/init-key :amble/move-receive [_ {:keys [app-atom, move-chan, move-resource-get!, latest-move-index-chan]}]
  (async/pipe latest-move-index-chan 
              amble-client.move-receive/latest-move-index-chan)
  (async/pipe  amble-client.move-receive/move-chan
               move-chan)
  (js/setTimeout (fn [& args]
                   (async/put! app-atom-chan app-atom))
                 1000) 

  (async/put! move-resource-get!-chan move-resource-get!)
  nil)


