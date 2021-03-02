(ns amble-client.resource.player
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get!
  "Return all of the players of a given game."
  ([game-id]
   (resource-core/response-chan {:endpoint-key :player-get-all-by-game-id
                                 :param-map {:game-id game-id}}))
  ([game-id id]
   "Return a player's current state of a given game."
   (resource-core/response-chan {:endpoint-key :player-get
                                 :param-map {:game-id game-id
                                             :id id}})))


