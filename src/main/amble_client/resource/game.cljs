(ns amble-client.resource.game
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get! [id]
  (resource-core/response-chan {:endpoint-key :game-get-by-id
                                :param-map {:id id}}))

(defn create! []
  (resource-core/response-chan {:endpoint-key :game-add
                                :param-map {}}))


