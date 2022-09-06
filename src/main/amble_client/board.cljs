(ns amble-client.board
  (:require [integrant.core :as ig]))

(defn board [board-pieces, player-pieces, game-play]
  (fn []
    [:svg {:id "board"
           "viewBox" "0 0 1 1"
           :on-mouse-move (fn [e]
                            (.preventDefault e)
                            (.persist e)
                            ((:handle-ui-event game-play)
                             e))}
     [board-pieces]
     [player-pieces]]))

(defmethod ig/init-key :amble/board [_ {:keys [board-pieces, player-pieces, game-play]}]
  (board board-pieces, player-pieces, game-play))


