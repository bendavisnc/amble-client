(ns amble-client.v3.board
  "Draws pieces on a board, according to state under a particular game id."
  (:require [amble-client.v3.piece :as piece]
            [amble-client.v3.global-state :as gs]))

(defn on-mouse-down []
  (println "hello from board!"))

(defn board [s, game-id]
  [:svg {:id "board" "viewBox" "0 0 1 1"}
    ;; Draw board landing pieces (the svg circles that designate where player pieces can "land").
    (for [i (range (count (gs/get s game-id :pieces :landing)))]
      [piece/landing-piece s  i {:on-mouse-down on-mouse-down}])
    ;; Draw player pieces.
    (for [player-id (keys (gs/get s game-id :pieces :player))]
      (for [i (range (count (gs/get s game-id :pieces :player player-id)))]
        [piece/player-piece s player-id i {:on-mouse-down on-mouse-down
                                           :on-mouse-up on-mouse-down}]))])
 
