(ns amble-client.board-piece-closest
  "Figures out which piece is closest, given some x y value."
  (:require [integrant.core :as ig]))

;; Looks at the kinda constant list of board coords (kinda because they're server values), and uses a 
;; sort function to return the nearest coord in that list to the coord given."
(defn board-piece-closest [app-atom]
  (fn [x, y]
    (first (sort-by (fn [board-piece]
                      (let [xxx (- (:x board-piece) x)
                            yyy (- (:y board-piece) y)]
                        (Math/sqrt (+ (* xxx xxx)
                                      (* yyy yyy)))))
                    (:board-pieces (deref app-atom))))))

(defmethod ig/init-key :amble/board-piece-closest [_ {:keys [app-atom]}]
  (board-piece-closest app-atom))



