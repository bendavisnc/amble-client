(ns amble-client.board
  (:require [integrant.core :as ig]))

(defn board [app-atom, board-pieces, player-pieces, game-play]
  (fn []
    (let [player-selected (or (get-in (deref app-atom) 
                                      [:settings :player])
                              :player-one)]
      [:div {:id "board-container"}
        [:svg {:id "board"
               :class (str (name player-selected)
                           "-sixoclock")
               "viewBox" "0 0 1 1"
               :on-mouse-move (fn [e]
                                (.preventDefault e)
                                (.persist e)
                                ((:handle-ui-event game-play)
                                 e))}
         [board-pieces]
         [player-pieces]]])))

(defmethod ig/init-key :amble/board [_ {:keys [app-atom, board-pieces, player-pieces, game-play]}]
  (board app-atom, board-pieces, player-pieces, game-play))


