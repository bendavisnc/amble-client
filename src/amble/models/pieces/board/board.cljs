(ns amble.models.pieces.board.board
  (:require
   [amble.static-content.board :as static-board]))

(defn closest [x y]
  (first
    (sort-by
      (fn [[x2 y2]]
        (let [dx (- x2 x)
              dy (- y2 y)]
          (+ (* dx dx) (* dy dy)))) ; no need for Math/sqrt when just comparing distance
      static-board/board)))
