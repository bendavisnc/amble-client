(ns amble-client.resource.move
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get! [game-id, player-id, move-id]
  (resource-core/response-chan {:endpoint-key :move-get-by-id
                                :param-map {:game-id game-id
                                            :player-id player-id
                                            :id move-id}}))


(defn add! [game-id, player-id, player-piece-index, move]
  (println "neat mr")
  (println [game-id, player-id, player-piece-index, move])
  (resource-core/response-chan {:endpoint-key :move-add
                                :param-map    {:game-id game-id
                                               :player-id player-id
                                               :player-piece-index player-piece-index
                                               :request {:body move}}}))
                                              ;;  :request {:body move}}}))

