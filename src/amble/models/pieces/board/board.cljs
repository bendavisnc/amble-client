(ns amble.models.pieces.board.board
  (:require
   [amble.static-content.board :as static-board]))

(defn closest [x, y]
  (first (sort-by (fn [[x2, y2]]
                      (let [x3 (- x2 x)
                            y3 (- y2 y)]
                        (Math/sqrt (+ (* x3 x3)
                                      (* y3 y3)))))
                  static-board/board)))
