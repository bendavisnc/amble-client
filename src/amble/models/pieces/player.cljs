(ns amble.models.pieces.player
  "Provides ui events that can cause a new move event to be sent to the server."
  (:require
   [amble.models.models :refer [db-to-game-id]]
   [amble.models.pieces.board.board :as board]
   [amble.server.server :as server]
   [re-frame.core :as re-frame]))

(defn- redraw [db, {:keys [player-id, index, x, y]}]
  ;; (println [player-id, index, x, y])
  ;; (println [(type player-id), (type index), (type x), (type y)])
  (assert (= (type :k)
             (type player-id))
          (str "`player-id` is not a keyword: " [player-id, (type player-id)]))
  (assert (= (type 0)
             (type index))
          (str "`index` is not a number: " [index, (type index)]))
  (assoc-in db [:game :player player-id :position index]
            [x, y]))

(defn- move-event-progress [db, {:keys [player-id, x, y]}]
  (update-in db
             [:game :player player-id :move-in-progress :moves]
             concat
             [[x, y]]))

(re-frame/reg-event-db
  ::on-player-move-add-success
  (fn [db [_ args]]
    (println "Player move added successfully" args)))

(re-frame/reg-event-db
  ::on-player-move-add-failure
  (fn [db [_ args]]
    (throw (new js/Error
                (str "Failed to add player move: " args)))))

;; Recursive loop for drawing moves. 
;; Used when remote moves happen.
(re-frame/reg-event-fx
  ::do-move-replay
  (fn [{:keys [db]}, [_ player-id, index, move-seq, wait-ms]]
    (cond (not (get-in db [:game :player player-id :move-replay-in-progress]))
          {:db (-> db
                   (assoc-in [:game :player player-id :move-replay-in-progress :move]
                             move-seq)
                   (assoc-in [:game :player player-id :move-replay-in-progress :index]
                             (js/parseInt index)))
           :dispatch [::do-move-replay player-id, index, move-seq, wait-ms]}

          (= []
             (get-in db [:game :player player-id :move-replay-in-progress :move]))
          {:db (update-in db
                          [:game :player player-id]
                          dissoc
                          :move-replay-in-progress)}

          :else
          (let [move-seq (get-in db [:game :player player-id :move-replay-in-progress :move])]
            ;; _ (println (get-in db [:game :player player-id]))] 
            {:db
             (-> db
                 (update-in [:game :player player-id :move-replay-in-progress :move]
                            (comp vec rest))
                 (redraw {:player-id player-id
                          :index (get-in db [:game :player player-id :move-replay-in-progress :index])
                          :x (first (first move-seq))
                          :y (second (first move-seq))}))

             :dispatch-later [{:ms wait-ms
                               :dispatch [::do-move-replay player-id, index, move-seq wait-ms]}]}))))

;; Coeffect handler that injects the current timestamp as :now
(re-frame/reg-cofx
  :now
  (fn [coeffects _]
    (assoc coeffects :now (js/Date.now))))

(re-frame/reg-event-fx
  ::on-player-move-get-success
  (fn [{:keys [db]}, [_ {:keys [body]}]]
    (let [{:keys [player-id, player-piece-index, move, x, y, client-id]} body
          move-seq move
          move-from-this-client? ((get-in db [:game :player (keyword player-id) :moves-made])
                                  client-id)]
      (when (not move-seq)
        (throw (new js/Error (str "No move sequence found in player move get response."
                                  body))))
      (if move-from-this-client?
        (do
          (println "Move from this client, skipping replay.")
          {})
        {:db db
         :dispatch [::do-move-replay
                    (keyword player-id)
                    (js/parseInt player-piece-index)
                    (concat move-seq [[x, y]])
                    ;; todo, name
                    24]}))))

(re-frame/reg-event-db
  ::on-player-move-get-failure
  (fn [db [_ args]]
    (throw (new js/Error
                (str "Failed to request player move: " args)))))

(re-frame/reg-event-db
  ::move-start
  (fn [db [_ {:keys [player-id, index, x, y]}]]
    (when (not index)
      (throw (new js/Error "No index found for player move, at move start.")))
    (-> db
        (assoc-in [:game :player player-id :move-in-progress :moves]
                  [[x, y]])
        (assoc-in [:game :player player-id :move-in-progress :index]
                  index)
        (update-in [:game]
          dissoc
          :landing-piece))))

(re-frame/reg-event-db
  ::move-update
  (fn [db [_ {:keys [player-id, index, x, y] :as move-event}]]
    (when (not index)
      (throw (new js/Error "No index found for player move, at move start.")))
    ;; (println [[player-id, index], (:game db)])
    (when (= index
             (get-in db [:game :player player-id :move-in-progress :index]))
      ;; (println "Updating move in progress for player" player-id "at index" index)
      (-> db
          (move-event-progress move-event)
          (redraw move-event)))))

(re-frame/reg-event-fx
  ::move-land
  (fn [{:keys [db]} [_ {:keys [player-id] :as move-event}]]
    (let [index  (get-in db [:game :player player-id :move-in-progress :index])]
      {:db (-> db
               (update-in [:game :player player-id]
                          dissoc
                          :move-in-progress)
               (assoc-in [:game :landing-piece]
                         {:player-id player-id
                          :index index})
               (redraw move-event))})))

(re-frame/reg-event-fx
  ::move-end
  [(re-frame/inject-cofx :now)]
  (fn [{:keys [db, now]} [_ {:keys [player-id]}]]
    (let [moves (get-in db [:game :player player-id :move-in-progress :moves])
          index  (get-in db [:game :player player-id :move-in-progress :index])
          [last-x last-y] (last moves)
          [x, y] (board/closest last-x, last-y)
          client-id (str now)
          move-event {:player-id player-id
                      :move moves
                      :x x
                      :y y
                      :index index
                      :client-id client-id
                      :game-id (db-to-game-id db)}]
      {:dispatch-n [[::move-land move-event]
                    [::server/player-move-add move-event ::on-player-move-add-success, ::on-player-move-add-failure]]
       :db (update-in db
                      [:game :player player-id :moves-made]
                      conj
                      client-id)})))

(re-frame/reg-event-fx
  ::on-move-remote
  (fn [_ [_ move-id]]
    (println "Received remote move with ID: " move-id)
    {:dispatch [::server/player-move-get move-id ::on-player-move-get-success, ::on-player-move-get-failure]}))

(comment (conj #{3} 2))
