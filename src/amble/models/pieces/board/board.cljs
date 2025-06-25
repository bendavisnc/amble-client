(ns amble.models.pieces.board.board
  (:require
   [goog.string :as gstring]))

(defn- distance-squared [{:keys [x y]} [x2 y2]]
  (let [dx (- x2 x)
        dy (- y2 y)]
    (+ (* dx dx) (* dy dy))))

(defn- available-squares-by-distance [{:keys [x y board occupied]}]
  (assert (pos-int? (count board))
          (gstring/format "Bogus board, %s" board))
  (let [occupied (or occupied #{})]
    (->> board
      (map-indexed vector)
      (remove (fn [[i _]] (occupied i)))
      (sort-by (fn [[_ pos]] (distance-squared {:x x :y y} pos))))))

(defn closest
  "Returns an x, y coordinate of the closest available square to the given state."
  [state]
  (some-> (available-squares-by-distance state)
          first
          second))

(defn closest-index
  "Returns an x, y coordinate's corresponding index of the closest available square to the given state."
  [state]
  (some-> (available-squares-by-distance state)
          first
          first))

(defn index [{:keys [x y board]}]
  (assert (pos-int? (count board))
          (gstring/format "Bogus board, %s" board))
  (->> board
       (map-indexed vector)
       (some (fn [[i xy]]
               (when (= xy [x y])
                 i)))))
