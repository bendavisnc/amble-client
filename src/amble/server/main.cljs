(ns amble.server.main
  "Implements the client api for server requests."
  (:require
   [amble.models.models :refer [db-to-game-id]]
   [amble.server.server :as server]
   [martian.re-frame :as martian-reframe]
   [re-frame.core :as re-frame]))

;; todo, remove constants
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
  ::server/delete-game
  (fn [{:keys [db]} [_ game-id, on-success, on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :game-delete-by-id
                {:id game-id}
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

;; Coeffect handler that injects the current timestamp as :now
(re-frame/reg-cofx
  :now
  (fn [coeffects _]
    (assoc coeffects :now (js/Date.now))))

(re-frame/reg-event-fx
  ::server/player-move-add
  [(re-frame/inject-cofx :now)]
  (fn [{:keys [db now]} [_
                         {:keys [game-id player-id index move x y]}
                         on-success
                         on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :move-add
                {:gameId game-id
                 :playerId player-id
                 :playerPieceIndex index
                 :body {:move move
                        :x x
                        :y y
                        :client-id (str now)}}
                [on-success]
                [on-failure]]}))

(re-frame/reg-event-fx
  ::server/player-move-get
  (fn [{:keys [db]} [_ id on-success, on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :move-get-by-id
                {:game-id (db-to-game-id db)
                 :id id}
                [on-success]
                [on-failure]]}))
