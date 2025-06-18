(ns amble.models.pieces.board.board
  (:require
   ;;  [amble.models.pieces.player :as player]
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

(re-frame/reg-event-db
  ::active-index
  (fn [db [_ {:keys [index]}]]
    (assoc-in db [:game :board :active-index] index)))

;; When a mouseevent happens on the svg board, include it in any move currently in progress.
(re-frame/reg-event-fx
  ::move-update
  (fn [{:keys [db]} [_, {:keys [x, y]}]]
    (let [mip (some (fn [[player-id player]]
                      (when-let [move (:move-in-progress player)]
                        {:player-id player-id
                         :index (:index move)
                         :x x
                         :y y}))
                    (get-in db [:game :player]))]

      (merge {:db db}
             (if mip
               {:dispatch-n [[:amble.models.pieces.player/move-update mip]
                             [::active-index {:index (closest-index {:x (:x mip)
                                                                     :y (:y mip)
                                                                     :occupied (get-in db [:game :board :occupied])})}]]}
               {})))))

(comment ([1, 2]
          1))
