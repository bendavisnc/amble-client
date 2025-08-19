(ns amble.server.main
  "Implements the client api for server requests."
  (:require
   [amble.config :as amble-config]
   [amble.models.models :refer [db-to-game-id]]
   [amble.server.server :as server]
   [goog.string :as gstring]
   [martian.re-frame :as martian-reframe]
   [re-frame.core :as re-frame]))

(let [openapi-url (gstring/format "%s/json/openapi.json" amble-config/CLIENT_URL)]
  (martian-reframe/init openapi-url {:server-url amble-config/SERVER_URL}))

(re-frame/reg-event-fx
  ::server/post-game
  (fn [{:keys [db]} [_ game-id, on-success, on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :game-add
                (if game-id
                  {:game-id game-id}
                  {})
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
  ::server/game-get-by-id
  (fn [{:keys [db]} [_, game-id, on-success, on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :game-get-by-id
                {:id game-id}
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

(re-frame/reg-event-fx
  ::server/player-move-add
  (fn [{:keys [db]} [_
                     {:keys [game-id player-id index move x y, client-id]}
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
                        :client-id client-id}}
                [on-success]
                [on-failure]]}))

(re-frame/reg-event-fx
  ::server/player-move-delete
  (fn [{:keys [db]} [_
                     {:keys [game-id id]}
                     on-success
                     on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :move-delete-by-id
                {:gameId game-id
                 :id id}
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

(re-frame/reg-event-fx
  ::server/player-move-get-all
  (fn [{:keys [db]} [_ id on-success, on-failure]]
    {:db db
     :dispatch [::martian-reframe/request
                :move-get-by-game-id
                {:game-id id}
                [on-success]
                [on-failure]]}))
