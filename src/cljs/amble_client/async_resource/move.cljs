(ns amble-client.async-resource.move
  (:require [amble-client.async-resource.base :as base]
            [integrant.core :as ig]))

(defmethod base/amble-async-request :move [_, game-id, player-id, move-id]
  (println "todo, this thing, v2")
  (println [game-id, player-id, move-id]))
;

(defmethod ig/init-key :amble/async-resource-move [_ {:keys []}])
