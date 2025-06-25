(ns amble.models.pieces.board.pieces
  (:require
   [amble.models.pieces.board.board :refer [closest-index]]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-db
  ::piece-occupied
  (fn [db [_ i]]
    (update-in db
               [:game :board :occupied]
               conj
               i)))

(re-frame/reg-event-db
  ::piece-unoccupied
  (fn [db [_ i]]
    (update-in db
               [:game :board :occupied]
               disj
               i)))

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
                    (get-in db [:game :player]))
          board (get-in db [:game :board :pieces])]

      (merge {:db db}
             (if mip
               {:dispatch-n [[:amble.models.pieces.player/move-update mip]
                             [::active-index {:index (closest-index {:x (:x mip)
                                                                     :y (:y mip)
                                                                     :board board
                                                                     :occupied (get-in db [:game :board :occupied])})}]]}
               {})))))

(comment (disj (set [1, 2])
               2))
