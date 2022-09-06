(ns amble-client.resource.board
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get! [game-id]
  (resource-core/response-chan {:endpoint-key :board-get-by-game-id
                                :param-map {:game-id game-id}}))

