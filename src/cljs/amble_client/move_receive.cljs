(ns amble-client.move-receive
  "Listens to moves to send off to the server."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def latest-move-index-chan (async/chan))
(def move-resource-get!-chan (async/chan))
(def app-atom-chan (async/chan))

(go-loop [move-resource-get! (async/<! move-resource-get!-chan)
          game-id (:game-id (deref (async/<! (app-atom-chan))))]
  (let [latest-move-index (async/<! latest-move-index-chan)
        _ (println (str "New move announced from server, index " latest-move-index "."))
        latest-move (async/<! (move-resource-get! game-id, latest-move-index))]
    (println "cool beans")
    (println latest-move))
  (recur move-resource-get!, game-id))

(defmethod ig/init-key :amble/move-recieve [_ {:keys [app-atom, move-resource-get!, latest-move-index-chan]}]
  (async/pipe latest-move-index-chan amble-client.move-receive/latest-move-index-chan)
  (async/put! app-atom-chan app-atom)
  (async/put! move-resource-get!-chan move-resource-get!)
  nil)


