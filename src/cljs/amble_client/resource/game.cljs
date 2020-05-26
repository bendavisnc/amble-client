(ns amble-client.resource.game
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get! [game-id]
  (resource-core/response-promise :get-game-by-id {:game-id game-id}))