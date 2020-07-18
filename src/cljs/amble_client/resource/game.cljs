(ns amble-client.resource.game
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get! [id]
  (resource-core/response-promise {:endpoint-key :game-get-by-id
                                   :param-map {:id id}}))

(defn create! []
  (resource-core/response-promise {:endpoint-key :game-create
                                   :param-map {}}))


