;; filepath: /home/ben/home/programming/amble/amble-client/src/amble/models/dev.cljs
(ns amble.dev
  (:require
   [amble.models.models :refer [db-to-game-id]]
   [amble.server.server :as server]
   [re-frame.core :as re-frame]))

(defn printdb
  "Prints the current db to the console."
  []
  (re-frame/dispatch [::printdb]))

(re-frame/reg-event-db
  ::printdb
  (fn [db, _]
    (println (:game db))))

(defn index-to-player [i]
  (case i
    0 :player-one
    1 :player-two
    2 :player-three
    3 :player-four
    4 :player-five
    5 :player-six))

(defn setplayer [i]
  (re-frame/dispatch [::setplayer i]))

(re-frame/reg-event-db
  ::setplayer
  (fn [db, [_ i]]
    (assoc-in db
              [:game :settings :player]
              (index-to-player i))))

(re-frame/reg-event-fx
  ::on-game-delete-failure
  (fn [coeff, [_ event]]
    (println "Failed to delete game: " event)))

(re-frame/reg-event-fx
  ::on-game-delete-success
  (fn [_, [_ event]]
    (println "Game deleted successfully: " event)))

(re-frame/reg-event-fx
  ::deletegame
  (fn [cofx [_ _]]
    (let [game-id (db-to-game-id (:db cofx))]
      {:dispatch [::server/delete-game game-id ::on-game-delete-success, ::on-game-delete-failure]})))

(defn deletegame [i]
  (re-frame/dispatch [::deletegame]))
