(ns amble-client.resource.move
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get! [id]
  (resource-core/response-promise :game-get-by-id {:game-id id}))

(defn add! [& {:keys [game-id, move]}]
  (resource-core/response-promise :move-add {}))


