(ns amble-client.v3.board
  (:require [amble-client.v3.piece :as piece]
            [amble-client.v3.global-state :as gs]))

(defn on-mouse-down []
  (println "hello from board!"))

(defn board [s, game-id]
  [:svg {:id "board" "viewBox" "0 0 1 1"}
    (for [i (range (count (gs/get s game-id :pieces :landing)))]
      [piece/piece s piece/piece-type-landing i {:on-mouse-down on-mouse-down
                                                 :on-mouse-up on-mouse-down}])])
