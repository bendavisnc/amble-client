(ns amble.models.pieces.player
  "Provides ui events that can cause a new move event to be sent to the server."
  (:require
   [amble.models.models :refer [db-to-game-id]]
   [amble.models.pieces.board.board :as board :refer [closest]]
   [amble.models.pieces.board.pieces :as board-pieces]
   [amble.server.server :as server]
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(defn- redraw [db, {:keys [player-id, index, x, y]}]
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

(re-frame/reg-event-fx
  ::move-replay-add
  (fn [{:keys [db]}, [_ replay]]
    (let [move-replay (get-in db [:game :move-replay-in-progress])]
      (if move-replay
        {:db (update-in db
                        [:game :move-replays]
                        conj replay)}
        {:db db
         :dispatch-n replay}))))

;; Recursive loop for drawing moves. 
;; Used when remote moves happen.
(re-frame/reg-event-fx
  ::do-move-replay
  (fn [{:keys [db]}, [_, {:keys [move-seq, player-id, index, wait-ms] :as e}]]
    (let [move-replay (get-in db [:game :move-replay-in-progress])]
      (cond
        (nil? move-replay)
        {:db (-> db
                 (assoc-in [:game :move-replay-in-progress] e)
                 (update-in [:game :move-replays]
                            (comp vec rest)))
         :dispatch [::do-move-replay e]}
        ;; A move replay is over.
        ;;   - dissoc the move from state
        ;;   - place the piece at its final move position
        ;;   - dispatch the next replay if there is one
        (= []
          (some-> move-replay :move-seq))
        (let [[x-last, y-last] (last move-seq)
              board (get-in db [:game :board :pieces])
              [x, y] (closest {:x x-last
                               :y y-last
                               :board board})]
          {:db (-> db
                   (update-in [:game]
                              dissoc
                              :move-replay-in-progress)
                   (redraw {:player-id player-id
                            :index index
                            :x x
                            :y y})) ;; todo, explain
           :dispatch-n (when-let [next-replay
                                  (first (get-in db [:game :move-replays]))]
                         (println (gstring/format "Dispatching next replay, `%s`" next-replay))
                         next-replay)})

        ;; A move replay is in progress.
        ;;   - remove the next position to use from state 
        ;;   - take the next move position from the move sequence
        (seq (some-> move-replay :move-seq))
        {:db (-> db
                 (update-in [:game :move-replay-in-progress :move-seq]
                            (comp vec rest))
                 (redraw {:player-id player-id
                          :index index
                          :x (get-in db [:game :move-replay-in-progress :move-seq 0 0])
                          :y (get-in db [:game :move-replay-in-progress :move-seq 0 1])}))

         :dispatch-later [{:ms wait-ms
                           :dispatch [::do-move-replay e]}]}
        :else
        (println "`do-move-replay` at rest.")))))

(re-frame/reg-cofx
  :now
  (fn [coeffects _]
    (assoc coeffects :now (js/Date.now))))

(re-frame/reg-event-fx
  ::on-player-move-get-success
  (fn [{:keys [db]}, [_ {:keys [body]}]]
    (let [move (-> body
                   (update :player-id keyword)
                   (update :player-piece-index #(js/parseInt %))
                   (update :x #(js/parseFloat %))
                   (update :y #(js/parseFloat %)))
          player-id (:player-id move)
          client-id (:client-id move)
          move-seq (:move move)
          x (:x move)
          y (:y move)
          player-piece-index (:player-piece-index move)
          move-from-this-client? ((get-in db [:game :player player-id :moves-made])
                                  client-id)
          [x-start, y-start] (first move-seq)
          board (get-in db [:game :board :pieces])
          [x-end, y-end] (closest {:x x
                                   :y y
                                   :board board})
          board-index-start (board/index {:board board
                                          :x x-start
                                          :y y-start})
          board-index-end (board/index {:board board
                                        :x x-end
                                        :y y-end})]
      (when (not move-seq)
        (throw (new js/Error (str "No move sequence found in player move get response."
                                  body))))
      (when (or (not board-index-start) (not board-index-end))
        (throw (new js/Error (str "remote move related board indexs not found: "
                                  [board-index-start, board-index-end]))))
      (merge {:db (-> db
                      (update-in [:game :move :history] conj move)
                      ;; (update-in [:game :move :index] inc))
                      (assoc-in [:game :move :index] (inc (count (get-in db
                                                                         [:game :move :history])))))
              :notifications {:text (gstring/format "Move completed! `%s`"
                                                    (name player-id))
                              :timeout 1000}}

             (if move-from-this-client?
               (do
                 (println "Move from this client, skipping replay.")
                 {})
               {:dispatch [::move-replay-add [[::do-move-replay
                                               {:player-id player-id
                                                :index player-piece-index
                                                :move-seq (concat move-seq [[x, y]])
                                                :wait-ms 24}]
                                              [::board-pieces/piece-unoccupied board-index-start]
                                              [::board-pieces/piece-occupied board-index-end]]]})))))

;; deletes on the server side cause corresponding event triggers. 
;; currently we just ignore the 404 that happens for the corresponding get request afterwards.
(re-frame/reg-event-db
  ::on-player-move-get-failure
  (fn [db [_ args]]
    (println (str "Failed to request player move: " args))))
;; (throw (new js/Error
;;             (str "Failed to request player move: " args)))))

(re-frame/reg-event-fx
  ::move-start
  (fn [{:keys [db]}, [_ {:keys [player-id, index]}]]
    (when (not index)
      (throw (new js/Error "No index found for player move, at move start.")))
    (let [[x, y] (get-in db [:game :player player-id :position index])
          board-index (some (fn [[i [bx, by]]]
                              (when (and (= bx x) (= by y))
                                i))
                            (map-indexed vector (get-in db [:game :board :pieces])))
          _ (when (not board-index)
              (throw (new js/Error
                          (str "No board index found for piece at position: " [x, y]))))]
      {:db (-> db
               (assoc-in [:game :player player-id :move-in-progress :moves]
                         [[x, y]])
               (assoc-in [:game :player player-id :move-in-progress :index]
                         index)
               (update-in [:game] dissoc :landing-piece))
       :dispatch [::board-pieces/piece-unoccupied board-index]})))

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
  (fn [{:keys [db]} [_ {:keys [player-id, x, y] :as move-event}]]
    (let [index  (get-in db [:game :player player-id :move-in-progress :index])
          board (get-in db [:game :board :pieces])
          board-index (board/index {:board board
                                    :x x
                                    :y y})]
      {:db (-> db
               (update-in [:game :player player-id]
                          dissoc
                          :move-in-progress)
               (assoc-in [:game :landing-piece]
                         {:player-id player-id
                          :index index})
               (redraw move-event))
       :dispatch [::board-pieces/piece-occupied board-index]})))

(re-frame/reg-event-fx
  ::move-end
  [(re-frame/inject-cofx :now)]
  (fn [{:keys [db, now]} [_ {:keys [player-id]}]]
    (let [moves (get-in db [:game :player player-id :move-in-progress :moves])
          index  (get-in db [:game :player player-id :move-in-progress :index])
          [last-x last-y] (last moves)
          board (get-in db [:game :board :pieces])
          [x, y] (closest {:x last-x
                           :y last-y
                           :board board
                           :occupied (get-in db [:game :board :occupied])})
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
       :db (-> db
               (update-in [:game :player player-id :moves-made]
                          conj
                          client-id)
               (assoc-in [:game :board :active-index] nil))})))

(re-frame/reg-event-fx
  ::on-move-remote
  (fn [_ [_ move-id]]
    (println "Received remote move with ID: " move-id)
    {:dispatch [::server/player-move-get move-id ::on-player-move-get-success, ::on-player-move-get-failure]}))

(comment (conj [3] 2))
