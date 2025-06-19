(ns amble.models.pieces.board.board
  (:require
   [amble.static-content.board :as static-board]))

(defn closest [{:keys [x, y, occupied]}]
  (let [[_ xy]
        (first
          (filter (fn [[i, _]]
                    (not (occupied i)))
                  (sort-by
                    (fn [[_ [x2 y2]]]
                      (let [dx (- x2 x)
                            dy (- y2 y)]
                        (+ (* dx dx) (* dy dy))))
                    (map-indexed vector static-board/board))))]
    xy))

(defn closest-index [{:keys [x, y, occupied]}]
  (let [[i _]
        (first
          (filter (fn [[i, _]]
                    (not (occupied i)))
                  (sort-by
                    (fn [[_ [x2 y2]]]
                      (let [dx (- x2 x)
                            dy (- y2 y)]
                        (+ (* dx dx) (* dy dy))))
                    (map-indexed vector static-board/board))))]
    i))
