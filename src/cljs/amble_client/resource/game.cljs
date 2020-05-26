(ns amble-client.resource.game
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get! [game-id]
  (resource-core/response-promise :game-get-by-id {:game-id game-id}))

(defn search! [game-tag]
  (resource-core/response-promise :game-search {:tag game-tag}))