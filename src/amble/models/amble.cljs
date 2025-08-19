(ns amble.models.amble
  (:require
   [amble.amble :refer [index-to-player player-to-index]]
   [amble.models.hash-params :as hash-params]
   [amble.models.models :refer [db-to-game-id]]
   [amble.models.pieces.board.board :as board]
   [amble.models.pieces.board.pieces :as board-pieces]
   [amble.server.async.server :as async-server]
   [amble.server.server :as server]
   [amble.static-content.board :as static-board]
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(re-frame/reg-cofx
  :board
  (fn [cofx _]
    (assoc cofx :board
           (vec static-board/board))))

(re-frame/reg-event-fx
  ::initialize
  [(re-frame/inject-cofx :board)
   hash-params/interceptor]
  (fn [{:keys [db, board, hash-params]} [_]]
    (let [board-pieces-seq board
          player-id (if-let [player-id-from-addressbar (some-> hash-params :player keyword)]
                      (do (println (gstring/format "Using `player` selected from address bar, `%s`"
                                                   player-id-from-addressbar))
                          player-id-from-addressbar)
                      (do (println "No player selection found, using default `:player-one`.")
                          :player-one))
          player-index (player-to-index player-id)
          game-id (if-let [game-id-from-addressbar (some-> hash-params :game-id keyword)]
                    (do (println (gstring/format "Using `game-id` from address bar, `%s`"
                                                 game-id-from-addressbar))
                        game-id-from-addressbar)
                    (do (println "No `game-id` found in address bar, passing null.")
                        nil))]
      {:db (-> db
               (assoc-in [:game :game-id] game-id)
               (assoc-in [:game :move :history] [])
               (assoc-in [:game :move-replays] [])
               (assoc-in [:game :move :index] 0)
               (assoc-in [:game :board :pieces]
                         board-pieces-seq)
               (assoc-in [:game :settings :player-index] player-index)
               (assoc-in [:game :board :occupied]
                         #{}))
       :dispatch [::server/post-game game-id ::on-post-game-success, ::on-post-game-failure]})))

(re-frame/reg-event-fx
  ::on-player-move-get-by-id-success
  (fn [{:keys [db]}, [_ {:keys [body]}]]
    (let [move (-> body
                   (update :player-id keyword)
                   (update :player-piece-index #(js/parseInt %))
                   (update :x #(js/parseFloat %))
                   (update :y #(js/parseFloat %)))]
      {:db (-> db
               (update-in [:game :move :history] conj move)
               (assoc-in [:game :move :index] (inc (count (get-in db
                                                                  [:game :move :history])))))})))

(re-frame/reg-event-fx
  ::on-player-move-get-by-id-failure
  (fn [_, _]
    (throw (new js/Error "unhandled player move get by id failure response"))))

(re-frame/reg-event-fx
  ::on-player-move-get-all-success
  (fn [{:keys [db]} [_ {:keys [body]}]]
    (let [move-ids body
          game-id (db-to-game-id db)]
      {:db db
       :dispatch-n (mapv (fn [move-id]
                           [::server/player-move-get
                            (str move-id)
                            ::on-player-move-get-by-id-success
                            ::on-player-move-get-by-id-failure])
                         move-ids)})))

(re-frame/reg-event-fx
  ::on-player-move-get-all-failure
  (fn [{:keys [db]} _]
    (throw (new js/Error "unhandled player move get all failure request"))))

;; `game id retrieved` -> `game ready`
(re-frame/reg-event-fx
  ::on-game-id-success
  (fn [{:keys [db]} [_, event]]
    (let [game-id (or (some-> event :body :game-id keyword)
                      (some-> event :body keyword))]
      (merge {:db (assoc-in db [:game :game-id] game-id)}
             {:dispatch-n [[::server/player-move-get-all game-id ::on-player-move-get-all-success ::on-player-move-get-all-failure]
                           [::on-game-ready nil]]}))))

(re-frame/reg-event-fx
  ::on-game-id-failure
  (fn [{:keys [db]} [_]]
    (throw (new js/Error "unhandled game id failure request"))))

(re-frame/reg-event-fx
  ::on-post-game-success
  (fn [{:keys [db]}, [_ event]]
    (cond (= 201 (:status event))
          (let [game-id (keyword (get-in event [:body :game-id]))]
            (merge {:db (assoc-in db [:game :game-id] game-id)}
                   {:dispatch [::on-game-ready nil]}))
          :else
          (throw (new js/Error ["unexpected response result on `::on-post-game-success`"
                                event])))))

(re-frame/reg-event-fx
  ::on-post-game-failure
  (fn [coeff, [_ event]]
    (cond (= 409 (:status event))
          (merge {:db (:db coeff)
                  :dispatch [::on-post-game-failure-conflict event]})
          :else
          (throw (new js/Error ["unexpected response result on `::on-post-game-failure`"
                                event])))))

(re-frame/reg-event-fx
  ::on-post-game-failure-conflict
  (fn [{:keys [db]} [_]]
    (merge
      {:db db}
      (if-let [game-id (get-in db [:game :game-id])]
        {:dispatch [::server/game-get-by-id game-id ::on-game-id-success, ::on-game-id-failure]}
        {:dispatch [::server/game-get-default-id ::on-game-id-success, ::on-game-id-failure]}))))

;; list of players -> player info applied to game
(re-frame/reg-event-fx
  ::on-players-success
  (fn [{:keys [db]} [_ event]]
    (let [game-id (db-to-game-id db)
          players (mapv keyword (:body event))]
      {:db db
       :fx (let [player-get-dispatches (mapv (fn [player]
                                               [:dispatch [::server/player-get-by-id [game-id player]
                                                           ::on-player-success
                                                           ::on-player-failure]])
                                             players)
                 dispatches (concat player-get-dispatches
                                    [[:dispatch [::on-players-success-all]]])
                 _ (assert (= (count dispatches)
                              7))
                 _ (assert (= [:dispatch [::on-players-success-all]]
                              (last dispatches)))]
             dispatches)})))

(re-frame/reg-event-fx
  ::on-players-success-all
  (fn [{:keys [db]} [_ event]]
    ;;  (throw (new js/Error ["unhandled `::on-players-success-all`"]))))
    ;;  (println "on-players-success-all")
    {:db db}))

(re-frame/reg-event-fx
  ::on-player-six-success
  (fn [{:keys [db]} [_ event]]
    ;;  (throw (new js/Error ["unhandled `::on-players-success-all`"]))))
    ;; (println "on-player-six-success")
    {:db db}))

(re-frame/reg-event-fx
  ::on-players-failure
  (fn [& args]
    (throw (new js/Error ["unhandled `::on-players-failure`", args]))))

(re-frame/reg-event-fx
  ::on-player-failure
  (fn [& args]
    (throw (new js/Error ["unhandled `::on-player-failure`", args]))))

(re-frame/reg-event-fx
  ::on-player-success
  (fn [{:keys [db]}, [_, [_, player-id], event]]
    (let [position* (:body event)
          position (mapv (fn [[xstr, ystr]]
                           [(js/parseFloat xstr)
                            (js/parseFloat ystr)])
                         position*)
          board (get-in db [:game :board :pieces])]
      (merge {:db (-> db
                      (assoc-in [:game :player player-id :moves-made]
                                #{})
                      (assoc-in [:game :player player-id :position]
                                position))}
             (if (= :player-six player-id)
               {:fx [[:dispatch [::on-player-six-success event]]]}
               {:fx []})
             {:dispatch-n (mapv (fn [[x, y]]
                                  (let [board-index (board/index {:board board
                                                                  :x x
                                                                  :y y})]
                                    [::board-pieces/piece-occupied board-index]))
                                position)}))))

;; Once we know the game id, we can load player position
(re-frame/reg-event-fx
  ::on-game-ready
  (fn [{:keys [db]} [_ _]]
    (let [game-id (db-to-game-id db)
          game-ready-notification (gstring/format "Game `%s` is ready!" (name game-id))]
      {:db db
       :dispatch-n [[::async-server/initialize game-id]
                    [::server/player-get-all-by-game-id game-id ::on-players-success ::on-players-failure]]
       :notifications {:text game-ready-notification
                       :timeout 500}})))

(re-frame/reg-event-fx
  ::move-index-dec
  (fn [{:keys [db]} [_ _]]
    (let [move-index (get-in db [:game :move :index])
          history (get-in db [:game :move :history])
          can-dec? (pos-int? move-index)
          move (when (and can-dec?
                          (seq history))
                 (nth history (dec move-index)))]
      (if move
        (let [[x-start, y-start] (first (:move move))
              [x-end, y-end] ((juxt :x :y) move)
              board (get-in db [:game :board :pieces])
              board-index-start (board/index {:board board
                                              :x x-start
                                              :y y-start})
              board-index-end (board/index {:board board
                                            :x x-end
                                            :y y-end})]
          {:db (update-in db [:game :move :index] dec)
           :dispatch [:amble.models.pieces.player/move-replay-add [[:amble.models.pieces.player/do-move-replay
                                                                    {:player-id (:player-id move)
                                                                     :index (:player-piece-index move)
                                                                     :move-seq (reverse (:move move))
                                                                     :wait-ms 24}]
                                                                   [::board-pieces/piece-unoccupied board-index-end]
                                                                   [::board-pieces/piece-occupied board-index-start]]]})
        {}))))

(re-frame/reg-event-fx
  ::move-index-inc
  (fn [{:keys [db]} [_ _]]
    (let [move-index (get-in db [:game :move :index])
          history (get-in db [:game :move :history])
          can-inc? (< move-index (count history))
          move (when (and can-inc?
                          (seq history))
                 (nth history move-index))]
      (if move
        (let [[x-start, y-start] (first (:move move))
              [x-end, y-end] ((juxt :x :y) move)
              board (get-in db [:game :board :pieces])
              board-index-start (board/index {:board board
                                              :x x-start
                                              :y y-start})
              board-index-end (board/index {:board board
                                            :x x-end
                                            :y y-end})]
          {:db (update-in db [:game :move :index] inc)
           :dispatch [:amble.models.pieces.player/move-replay-add [[:amble.models.pieces.player/do-move-replay
                                                                    {:player-id (:player-id move)
                                                                     :index (:player-piece-index move)
                                                                     :move-seq (concat (:move move)
                                                                                 [[x-end, y-end]])
                                                                     :wait-ms 24}]
                                                                   [::board-pieces/piece-unoccupied board-index-start]
                                                                   [::board-pieces/piece-occupied board-index-end]]]})
        {}))))

(re-frame/reg-sub
  ::player-selected
  (fn [db, _]
    (let [player-index* (get-in db [:game :settings :player-index])
          players-count (count (get-in db [:game :player]))
          player-index (mod player-index* players-count)
          player-id (index-to-player player-index)]
      (assert (or (nil? player-id)
                  (keyword? player-id))
              (str "Expected player-selected to be a keyword, got: " [(type player-id)
                                                                      player-id]))
      player-id)))

(re-frame/reg-sub
  ::players
  (fn [db, _]
    (get-in db [:game :player])))

(re-frame/reg-sub
  ::board
  (fn [db, _]
    (get-in db [:game :board])))

(re-frame/reg-sub
  ::landing-piece
  (fn [db, _]
    (get-in db [:game :landing-piece])))

(re-frame/reg-sub
  ::player-index
  (fn [db, _]
    (get-in db [:game :settings :player-index])))

(re-frame/reg-sub
  ::move-index
  (fn [db, _]
    (get-in db [:game :move :index])))

(re-frame/reg-sub
  ::undo-disabled
  (fn [db, _]
    (let [move-index (get-in db [:game :move :index])]
      (zero? move-index))))

(re-frame/reg-sub
  ::redo-disabled
  (fn [db, _]
    (let [move-index (get-in db [:game :move :index])
          history (get-in db [:game :move :history])]
      (>= move-index (count history)))))

(re-frame/reg-sub
  ::amble
  (fn []
    [(re-frame/subscribe [::board])
     (re-frame/subscribe [::player-selected])
     (re-frame/subscribe [::player-index])
     (re-frame/subscribe [::players])
     (re-frame/subscribe [::landing-piece])
     (re-frame/subscribe [::move-index])
     (re-frame/subscribe [::undo-disabled])
     (re-frame/subscribe [::redo-disabled])])
  (fn [[board, player-selected, player-index, players, landing-piece, move-index, undo-disabled, redo-disabled]]
    {:board board
     :player-selected player-selected
     :player-index player-index
     :players players
     :landing-piece landing-piece
     :move-index move-index
     :undo-disabled undo-disabled
     :redo-disabled redo-disabled}))
