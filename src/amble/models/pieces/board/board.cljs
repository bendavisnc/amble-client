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

(re-frame/reg-event-db
  ::active-index
  (fn [db [_ {:keys [index]}]]
    (assoc-in db [:game :board :active-index] index)))

(re-frame/reg-event-fx
  ::move-update
  (fn [{:keys [db]} [_, event]]
    (let [mip (some (fn [[player-id player]]
                      (when-let [move (:move-in-progress player)]
                        {:player-id player-id
                         :index (:index move)
                         :x (:x event)
                         :y (:y event)}))
                    (get-in db [:game :player]))]

      (merge {:db db}
             (if mip
               {:dispatch [:amble.models.pieces.player/move-update mip]}
               {})))))

(comment (some identity [1, 2]))
