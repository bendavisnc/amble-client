(ns amble-client.resource.players
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get! [game-id]
  (resource-core/response-chan {:endpoint-key :player-get-all-by-game-id
                                :param-map {:game-id game-id}}))

