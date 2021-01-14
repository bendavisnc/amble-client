(ns amble-client.core
  (:require
   [amble-client.resource.game :as game-resource]
   [amble-client.resource.player :as player-resource]
   [amble-client.resource.board :as board-resource]
   [amble-client.board]
   [amble-client.board-pieces]
   [reagent.dom :as reagent-dom]
   [amble-client.utils :as utils]
   [cljs.core.async :as async]
   [integrant.core :as ig])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(defn app [board]
  [:div {:id "amble"} board])

(defn init! []
  (println "Starting client init!")
  (let [ig-config {:amble/board {:board-pieces (ig/ref :amble/board-pieces)}
                   :amble/board-pieces {:game-id (utils/game-id-from-window)
                                        :resource-chan-fn board-resource/get!}}
                   ;:amble/players {:game-id-fn utils/game-id-from-window
                   ;                :resource-chan-fn player-resource/get!}}]

        ig-amble (ig/init ig-config)]
    (go (let [board (async/<! (:amble/board ig-amble))]
          (println "Invoking reagent.")
          (reagent-dom/render (app board)
                              (.getElementById js/document "app"))))

    nil))

(defn post-game! []
  (async/take! (game-resource/create!)
               (fn [game-create-response]
                 (println "Requested new game.")
                 (println js/console game-create-response))))



