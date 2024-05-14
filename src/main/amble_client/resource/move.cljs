(ns amble-client.resource.move
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get! 
  ([game-id]
   (resource-core/response-chan {:endpoint-key :move-get-by-game-id
                                 :param-map {:game-id game-id}}))
  ([game-id, move-id]
   (resource-core/response-chan {:endpoint-key :move-get-by-id
                                 :param-map {:game-id game-id
                                             :id move-id}})))

(defn add! [game-id, player-id, player-piece-index, move, x, y, client-id]
  (resource-core/response-chan {:endpoint-key :move-add
                                :param-map {:game-id game-id
                                            :player-id player-id
                                            :player-piece-index player-piece-index
                                            :move move
                                            :x x
                                            :y y
                                            :client-id client-id}}))

(defn delete! [game-id, id]
  (resource-core/response-chan {:endpoint-key :move-delete-by-id
                                :param-map {:game-id game-id
                                            :id id}}))




