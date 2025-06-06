(ns amble.models.pieces.player
  (:require
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
  (fn [db [_ {:keys [player, x, y]}]]
    (-> db
        (assoc-in [:game :player player :move-in-progress :moves]
                  [[x, y]]))))

(defn db-to-game-id  [db]
  (let [game-id (get-in db [:game :game-id])
        _ (when (not game-id)
            (println (:game db))
            (throw (new js/Error "No game id found.")))]
    (println (:game db))
    game-id))

(re-frame/reg-event-fx
  ::move-end
  (fn [{:keys [db]} [_ {:keys [player, x, y]}]]
    (let [moves (-> db (get-in [:game :player player :move-in-progress :moves]))
          move-event {:player-id player
                      :move moves
                      :x x
                      :y y
                      :index 1 ;; todo 
                      :client-id "stilltodoclientid"
                      :game-id (db-to-game-id db)}]
      {:dispatch [::server/player-move-add move-event ::on-player-move-add-success, ::on-player-move-add-failure]
       :db (update-in db
                      [:game :player player]
                      dissoc
                      :move-in-progress)})))
