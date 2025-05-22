(ns amble.models.amble
  (:require
   [amble.server.server :as server]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::initialize
 (fn [{:keys [db]} [_]]
   (merge
    {:db (assoc db :player-selected :player-one)}
    {:dispatch [::server/post-game ::server/post-game-success, ::server/post-game-failure]})))

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
