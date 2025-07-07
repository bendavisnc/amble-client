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
                      (do (println (gstring/format "Using player selected from address bar, `%s`"
                                                   player-id-from-addressbar))
                          player-id-from-addressbar)
                      (do (println "No player selection found, using default `:player-one`.")
                          :player-one))
          player-index (player-to-index player-id)]
      {:db (-> db
               (assoc-in [:game :move :history] [])
               (assoc-in [:game :move :index] 0)
               (assoc-in [:game :board :pieces]
                         board-pieces-seq)
               (assoc-in [:game :settings :player-index] player-index)
               (assoc-in [:game :board :occupied]
                         #{}))
       :dispatch [::server/post-game ::on-post-game-success, ::on-post-game-failure]})))

;; `game id retrieved` -> `game ready`
(re-frame/reg-event-fx
  ::on-game-id-success
  (fn [{:keys [db]} [_, event]]
    (let [game-id (keyword (:body event))]
      (merge {:db (assoc-in db [:game :game-id] game-id)}
             {:dispatch [::on-game-ready nil]}))))

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

;; When a game post fails, just ask what `game-id` to go with.
(re-frame/reg-event-fx
  ::on-post-game-failure-conflict
  (fn [{:keys [db]} [_]]
    (merge
      {:db db}
      {:dispatch [::server/game-get-default-id ::on-game-id-success, ::on-game-id-failure]})))

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
    (let [game-id (db-to-game-id db)]
      {:db db
       :dispatch-n [[::async-server/initialize game-id]
                    [::server/player-get-all-by-game-id game-id ::on-players-success ::on-players-failure]]
       :notifications {:text "Game is ready!"
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
              [x-end, y-end] (last (:move move))
              board (get-in db [:game :board :pieces])
              board-index-start (board/closest-index {:board board
                                                      :x x-start
                                                      :y y-start})
              board-index-end (board/closest-index {:board board
                                                    :x x-end
                                                    :y y-end})]
          {:db (update-in db [:game :move :index] dec)
           :dispatch-n [[:amble.models.pieces.player/do-move-replay
                         (:player-id move)
                         (:player-piece-index move)
                         (reverse (:move move))
                         24]
                        [::board-pieces/piece-unoccupied board-index-end]
                        [::board-pieces/piece-occupied board-index-start]]})
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
           :dispatch-n [[:amble.models.pieces.player/do-move-replay
                         (:player-id move)
                         (:player-piece-index move)
                         (concat (:move move)
                                 [[x-end, y-end]])
                         24]
                        [::board-pieces/piece-unoccupied board-index-start]
                        [::board-pieces/piece-occupied board-index-end]]})
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
  ::amble
  (fn []
    [(re-frame/subscribe [::board])
     (re-frame/subscribe [::player-selected])
     (re-frame/subscribe [::player-index])
     (re-frame/subscribe [::players])
     (re-frame/subscribe [::landing-piece])])
  (fn [[board, player-selected, player-index, players, landing-piece]]
    {:board board
     :player-selected player-selected
     :player-index player-index
     :players players
     :landing-piece landing-piece}))
