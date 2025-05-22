(ns amble.server.main
  (:require [re-frame.core :as re-frame]
            [martian.re-frame :as martian-reframe]
            [amble.server.server :as server]))  


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
  (fn [{:keys [db]} [_ on-success, on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :player-get-all-by-game-id
                {:game-id (:game-id db)}
                [on-success] 
                [on-failure]]})) 

(re-frame/reg-event-fx
  ::server/player-get-by-id
  (fn [{:keys [db]} [_ id on-success, on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :player-get
                {:game-id (:game-id db)
                 :id id}
                [on-success id] 
                [on-failure id]]})) 
