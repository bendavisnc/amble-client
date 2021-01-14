(ns amble-client.core
  (:require [cljs.core.async :as async]
            [integrant.core :as ig]
            [reagent.dom :as reagent-dom]
            [amble-client.board-pieces]
            [amble-client.board]
            [amble-client.player-pieces]
            [amble-client.resource.board :as board-resource]
            [amble-client.resource.game :as game-resource]
            [amble-client.resource.player :as player-resource]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go]]))

(defn  ig-amble-config [game-id]
  {:amble/board   {:board-pieces (ig/ref :amble/board-pieces)
                   :player-pieces (ig/ref :amble/player-pieces)}
   :amble/board-pieces {:game-id          game-id
                        :resource-chan-fn board-resource/get!}
   :amble/player-pieces {:game-id          game-id
                         :resource-chan-fn player-resource/get!}})

(defn app [board]
  [:div {:id "amble"} board])

(defn init! []
  (println "Starting client init!")
  (go (let [game-id (utils/game-id-from-window)
            ig-amble (ig/init (ig-amble-config game-id))
            board (async/<! (:amble/board ig-amble))]
        (println "Invoking reagent.")
        (reagent-dom/render (app board)
                            (.getElementById js/document "app"))
        nil)))

(defn post-game! []
  (async/take! (game-resource/create!)
               (fn [game-create-response]
                 (println "Requested new game.")
                 (println game-create-response))))



