(ns amble-client.resource.move
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get! [id]
  (resource-core/response-chan {:endpoint-key :game-get-by-id
                                :param-map {:game-id id}}))

(defn add! [game-id, player-id, move]
  (resource-core/response-chan {:endpoint-key :move-add
                                :param-map    {:game-id game-id
                                               :player-id player-id
                                               :move move}}))

