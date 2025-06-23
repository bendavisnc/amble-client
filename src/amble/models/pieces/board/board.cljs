(ns amble.models.pieces.board.board
  (:require
   [amble.static-content.board :as static-board]))

(defn- distance-squared [{:keys [x y]} [x2 y2]]
  (let [dx (- x2 x)
        dy (- y2 y)]
    (+ (* dx dx) (* dy dy))))

(defn- available-squares-by-distance [{:keys [x y occupied]}]
  (let [occupied (or occupied #{})]
    (->> static-board/board
      (map-indexed vector)
      (remove (fn [[i _]] (occupied i)))
      (sort-by (fn [[_ pos]] (distance-squared {:x x :y y} pos))))))

(defn closest [state]
  (some-> (available-squares-by-distance state)
          first
          second))

(defn closest-index [state]
  (some-> (available-squares-by-distance state)
          first
          first))
