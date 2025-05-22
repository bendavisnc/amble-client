(ns amble.models.amble
  (:require
   [amble.server.server :as server]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::initialize
 (fn [{:keys [db]} [_]]
   (merge
    {:db (assoc db :player-selected :player-one)}
    {:dispatch [::server/post-game ::on-post-game-success, ::on-post-game-failure]})))

(re-frame/reg-event-fx
 ::on-post-game-success
 (fn [{:keys [db]} [_]]
   (throw (new js/Error "to do soon, also"))))

(re-frame/reg-event-fx
 ::on-game-id-success
 (fn [{:keys [db]} [_]]
   (throw (new js/Error "to do soon, game id"))))

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

(re-frame/reg-event-fx
 ::on-post-game-failure-conflict
 (fn [{:keys [db]} [_]]
   (merge
    {:db db}
    {:dispatch [::server/game-get-default-id ::on-game-id-success, ::on-game-id-failure]})))

(re-frame/reg-sub
 ::player-selected
 (fn [db, _]
   (:player-selected db)))

(re-frame/reg-sub
 ::amble
 (fn []
   [(re-frame/subscribe [::player-selected])])
 (fn [[player-selected]]
   {:player-selected player-selected}))
