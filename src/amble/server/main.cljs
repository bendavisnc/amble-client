(ns amble.server.main
  (:require
   [amble.server.server :as server]
   [martian.re-frame :as martian-reframe]
   [re-frame.core :as re-frame]))

(martian-reframe/init "http://localhost:9500/json/openapi.json" {:server-url "http://localhost:3000"})

(re-frame/reg-event-fx
  ::server/post-game
  (fn [{:keys [db]} [_ on-success, on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :game-add
                {}
                [on-success]
                [on-failure]]}))

(re-frame/reg-event-fx
  ::server/game-get-default-id
  (fn [{:keys [db]} [_ on-success, on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :game-get-default-id
                {}
                [on-success]
                [on-failure]]}))

(re-frame/reg-event-fx
  ::server/player-get-all-by-game-id
  (fn [{:keys [db]} [_, game-id, on-success, on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :player-get-all-by-game-id
                {:game-id game-id}
                [on-success]
                [on-failure]]}))

(re-frame/reg-event-fx
  ::server/player-get-by-id
  (fn [{:keys [db]} [_, [game-id, player-id], on-success, on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :player-get
                {:game-id game-id
                 :id player-id}
                [on-success [game-id, player-id]]
                [on-failure]]}))
