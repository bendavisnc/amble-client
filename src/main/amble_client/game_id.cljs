(ns amble-client.game-id
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            ["react-router-dom" :as react-router-dom]
            [amble-client.resource.game :as game-resource])
  (:require-macros [cljs.core.async :refer [go]]))

(def app-atom-chan (async/chan))
(def app-ready-chan (async/chan))
(def game-id-clientside-chan (async/chan))

(defn- game-id []
  (fn []
    [:f> (fn []
           (let [params (react-router-dom/useParams)
                 game-id* (.-gameId params)]
             (assert (not (nil? game-id*)) 
                     "No game id from url params found.")
             (async/put! game-id-clientside-chan game-id*)
             [:<>]))]))



(go (let [app-atom (async/<! app-atom-chan)
          game-id-clientside (async/<! game-id-clientside-chan)
          get-response (async/<! (game-resource/get! game-id-clientside))
          game-id-existing (:game-id get-response)
          game-id-created (if game-id-existing
                            (println "Going with existing game.")
                            (do (println "Creating new game.")
                              (:game-id (async/<! (game-resource/create!)))))
          game-id (or game-id-existing
                      game-id-created
                      (throw (new js/Error "Could not obtain game id from server.")))] 
      (assert (= game-id-clientside game-id)
              (str "Unexpected game id, "
                   game-id))
      (swap! app-atom assoc :game-id game-id)
      (async/>! app-ready-chan true)))

(defmethod ig/init-key :amble/game-id [_ {:keys [app-atom, app-ready-chan]}]
  (async/put! app-atom-chan app-atom)
  (async/pipe amble-client.game-id/app-ready-chan app-ready-chan)
  (game-id))


