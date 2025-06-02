(ns amble.views.pieces.board
  (:require
   [amble.views.pieces.piece :as piece]))
(def classname "board-piece")

(defn pieces [pieces-seq]
  [:<>
   (for [{:keys [x, y, is-active?, index]} pieces-seq]
     (piece/piece :x x
                  :y y
                  :size piece/piece-size
                  :id (str classname "-" index)
                  :class (str classname
                              (if is-active? " active" ""))))])
