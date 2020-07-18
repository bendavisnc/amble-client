(ns amble-client.resource.move
  (:require
   [amble-client.resource.core :as resource-core]))

(defn get! [id]
  (resource-core/response-promise {:endpoint-key :game-get-by-id
                                   :param-map {:game-id id}}))

(defn add! [& {:keys [game-id, move]}]
  (resource-core/response-promise {:endpoint-key :move-add
                                   :param-map {:game-id game-id}
                                   :request-body move}))


