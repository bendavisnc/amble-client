(ns amble.models.pieces.board.board
  (:require
   [amble.static-content.board :as static-board]
   [re-frame.core :as re-frame]))

(defn closest [x y]
  (first
    (sort-by
      (fn [[x2 y2]]
        (let [dx (- x2 x)
              dy (- y2 y)]
          (+ (* dx dx) (* dy dy)))) ; no need for Math/sqrt when just comparing distance
      static-board/board)))

(re-frame/reg-event-db
  ::move-update
  (fn [db [_ {:keys [x, y] :as move-event}]]
    (println ["todo: move-update" move-event])))