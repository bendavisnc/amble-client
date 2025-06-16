(ns amble.models.amble
  (:require
   [amble.models.models :refer [db-to-game-id]]
   [amble.models.pieces.player :as player]
   [amble.server.async.server :as async-server]
   [amble.server.server :as server]
   [amble.static-content.board :as static-board]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
  ::initialize
  (fn [{:keys [db]} [_]]
    (let [board-pieces-seq (vec static-board/board)]
      (merge
        {:db (assoc-in db
                       [:game :board :pieces]
                       board-pieces-seq)}
        {:dispatch [::server/post-game ::on-post-game-success, ::on-post-game-failure]}))))

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
    (let [position (:body event)]
      (merge {:db (-> db
                      (assoc-in [:game :player player-id :moves-made]
                                #{})
                      (assoc-in [:game :player player-id :position]
                                (mapv (fn [[xstr, ystr]]
                                        [(js/parseFloat xstr)
                                         (js/parseFloat ystr)])
                                      position)))}
             (if (= :player-six player-id)
               {:fx [[:dispatch [::on-player-six-success event]]]}
               {:fx []})))))

;; Once we know the game id, we can load player position
(re-frame/reg-event-fx
  ::on-game-ready
  (fn [{:keys [db]} [_ _]]
    (let [game-id (db-to-game-id db)]
      {:db db
       :dispatch-n [[::async-server/initialize game-id]
                    [::server/player-get-all-by-game-id game-id ::on-players-success ::on-players-failure]]})))

(re-frame/reg-sub
  ::player-selected
  (fn [db, _]
    (or (:player-selected db)
        :player-one)))

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
  ::amble
  (fn []
    [(re-frame/subscribe [::board])
     (re-frame/subscribe [::player-selected])
     (re-frame/subscribe [::players])
     (re-frame/subscribe [::landing-piece])])
  (fn [[board, player-selected, players, landing-piece]]
    {:board board
     :player-selected player-selected
     :players players
     :landing-piece landing-piece}))
