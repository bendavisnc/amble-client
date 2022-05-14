(ns amble-client.board
  (:require [integrant.core :as ig]))

(defn board [board-pieces, player-pieces]
  (fn []
    [:svg {:id "board" "viewBox" "0 0 1 1"}
      [board-pieces]]))
      ;; [player-pieces]]))


(defmethod ig/init-key :amble/board [_ {:keys [board-pieces, player-pieces]}]
  (board board-pieces, player-pieces))


