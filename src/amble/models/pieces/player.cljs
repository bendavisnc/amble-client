(ns amble.models.pieces.player
  "Provides ui events that can cause a new move event to be sent to the server."
  (:require
   [amble.models.models :refer [db-to-game-id]]
   [amble.server.server :as server]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-db
  ::on-player-move-add-success
  (fn [db [_ args]]
    (println "Player move added successfully" args)))

(re-frame/reg-event-db
  ::on-player-move-add-failure
  (fn [db [_ args]]
    (throw (new js/Error
                (str "Failed to add player move: " args)))))

(re-frame/reg-event-db
  ::move-start
  (fn [db [_ {:keys [player-id, index, x, y]}]]
    (when (not index)
      (throw (new js/Error "No index found for player move, at move start.")))
    (-> db
        (assoc-in [:game :player player-id :move-in-progress :moves]
                  [[x, y]])
        (assoc-in [:game :player player-id :move-in-progress :index]
                  index))))

(re-frame/reg-event-db
  ::move-update
  (fn [db [_ {:keys [player-id, index, x, y]}]]
    (when (not index)
      (throw (new js/Error "No index found for player move, at move start.")))
    ;; (println [[player-id, index], (:game db)])
    (when (= index
             (get-in db [:game :player player-id :move-in-progress :index]))
      (println "Updating move in progress for player" player-id "at index" index)
      (-> db
          (update-in [:game :player player-id :move-in-progress :moves]
            concat
            [[x, y]])
          (assoc-in [:game :player player-id :position index]
                    [x, y])))))

(re-frame/reg-event-fx
  ::move-end
  (fn [{:keys [db]} [_ {:keys [player-id, x, y]}]]
    (let [moves (get-in db [:game :player player-id :move-in-progress :moves])
          index  (get-in db [:game :player player-id :move-in-progress :index])
          move-event {:player-id player-id
                      :move moves
                      :x x
                      :y y
                      :index index
                      :client-id "stilltodoclientid"
                      :game-id (db-to-game-id db)}]
      {:dispatch [::server/player-move-add move-event ::on-player-move-add-success, ::on-player-move-add-failure]
       :db (update-in db
                      [:game :player player-id]
                      dissoc
                      :move-in-progress)})))
