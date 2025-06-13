(ns amble.views.pieces.board
  (:require
   [amble.models.pieces.board.board :as board]
   [amble.views.pieces.piece :as piece]))
(def classname "board-piece")

(defn pieces [{:keys [pieces-seq, active-index]}]
  [:<>
   (for [[index, [x, y]] (map-indexed vector pieces-seq)
         :let [is-active? (= index active-index)]]
     (piece/piece :x x
                  :y y
                  :size piece/piece-size
                  :id (str classname "-" index)
                  :class (str classname (if is-active? " active" ""))
                  :extra-opts {:data-index index}))])
