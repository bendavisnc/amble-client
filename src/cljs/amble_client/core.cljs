(ns amble-client.core
  (:require
   [reagent.dom :as rdom]
   [amble-client.resource.game :as game-resource]
   [amble-client.resource.player :as player-resource]
   [amble-client.resource.board :as board-resource]
   [amble-client.markup.core :as markup]
   [amble-client.utils :as utils]
   [amble-client.state :as amble-client-state]
   [amble-client.interaction.core :as amble-client-interaction]
   [cljs.core.async :as casync])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(defn init!* []
  (go
    (try
      (let [game-id (utils/game-id-from-window)
            board-coords (:body (utils/error-checked (casync/<! (board-resource/get! game-id))))
            players (:body (utils/error-checked (casync/<! (player-resource/get! game-id))))
            _ (assert (< 0
                         (count players))
                      "Game has no players. What a sad day.")
            player-coords (casync/<! (casync/merge
                                       (for [player-id players]
                                         (casync/pipe
                                                      (player-resource/get! game-id player-id)
                                                      (casync/chan 2 (map :body))))))]

        (println "hey neat")
        (println players)
        (println player-coords)
        (println (count player-coords))
        (println (first player-coords))
        (println (second player-coords))
        (amble-client-state/init! :game-id game-id
                                  :designatee-coords board-coords
                                  :piece-indexes []))
      (catch js/Error error
        (amble-client-state/update! :errors [error])))))

(defn init! []
  (println "Starting client init.")
  (rdom/render
   [markup/app-markup-error-checked]
   (.getElementById js/document "app")
   init!*))

(defn post-game! []
  (casync/take! (game-resource/create!)
                (fn [game-create-response]
                  (println "Requested new game.")
                  (println js/console game-create-response))))
