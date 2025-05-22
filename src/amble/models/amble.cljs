(ns amble.models.amble
  (:require
   [amble.server.server :as server]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::initialize
 (fn [{:keys [db]} [_]]
   (merge
    {:db db}
    {:dispatch [::server/post-game ::on-post-game-success, ::on-post-game-failure]})))

(re-frame/reg-event-fx
 ::on-post-game-success
 (fn [{:keys [db]} [_]]
   (throw (new js/Error "to do soon, also"))))

(re-frame/reg-event-fx
 ::on-game-id-success
 (fn [{:keys [db]} [_, event]]
   (let [game-id (keyword (:body event))]
     (merge {:db (assoc-in db [:game game-id] {:game-id game-id})}
            {:dispatch [::on-game-ready event]}))))

(re-frame/reg-event-fx
 ::on-game-id-failure
 (fn [{:keys [db]} [_]]
   (throw (new js/Error "unhandled game id failure request"))))

(re-frame/reg-event-fx
 ::on-post-game-failure
 (fn [coeff, [_ event]]
   (println [(:status event)
             (= 409 (:status event))])
   (cond (= 409 (:status event))
         (do (println "Proceeding after game already exists conflict")
             (merge {:db (:db coeff)
                     :dispatch [::on-post-game-failure-conflict event]}))
         :else
         (throw (new js/Error ["unexpected response result on `:post-game`"
                               event])))))

;; If we can't just post a new game from nothing, we need to get the default game id.
(re-frame/reg-event-fx
 ::on-post-game-failure-conflict
 (fn [{:keys [db]} [_]]
   (merge
    {:db db}
    {:dispatch [::server/game-get-default-id ::on-game-id-success, ::on-game-id-failure]})))

(re-frame/reg-event-fx
 ::on-players-success
 (fn [{:keys [db]} [_ event]]
   (let [game-id (first (keys (:game db)))
         players (mapv keyword (:body event))]
     {:db db
      :fx (mapv (fn [player]
                  [:dispatch [::server/player-get-by-id [game-id player]
                              ::on-player-success
                              ::on-player-failure]])
                players)})))

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
;;  (fn [& args]
  ;;  (throw (new js/Error ["unhandled `::on-player-success`", args]))))
 (fn [coeff [_, [game-id, player-id], event]]
   (let [position (:body event)]
     (merge {:db (assoc-in (:db coeff)
                           [:game game-id player-id :position]
                           position)}))))

;; Once we know the game id, we can load player position
(re-frame/reg-event-fx
 ::on-game-ready
 (fn [{:keys [db]} [_, event]]
   (cond (and (= 200 (:status event))
              (:game db))
         (let [game-id (first (keys (:game db)))]
           (merge {:db db}
                  {:dispatch [::server/player-get-all-by-game-id game-id ::on-players-success, ::on-players-failure]}))
         :else
         (throw (new js/Error ["unhandled game ready", event])))))


(re-frame/reg-sub
 ::player-selected
 (fn [db, _]
   (:player-selected db)))

(re-frame/reg-sub
 ::amble
 (fn [db, _]
   (:game db)))
;;  (:game db)))
