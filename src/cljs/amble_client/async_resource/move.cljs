(ns amble-client.async-resource.move
  (:require [amble-client.async-resource.base :as base]
            [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(defmethod ig/init-key :amble/async-resource-move [_ {:keys [resource-chan-move-get]}]
  ;; This is called everytime a move is made remotely.
  ;;   Not top level because needs params.
  ;;     Maybe adjust this later.
  (let [chan (async/chan 1)]
    (defmethod base/amble-async-request-response :move [_, game-id, player-id, move-id]
      (go (let [move (async/<! (resource-chan-move-get game-id, player-id, move-id))
                _ (println "fuck yeah?!!")
                _ (println move)]
            (async/>! chan move))))
    chan))
