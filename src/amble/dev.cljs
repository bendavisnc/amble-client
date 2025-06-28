;; filepath: /home/ben/home/programming/amble/amble-client/src/amble/models/dev.cljs
(ns amble.dev
  (:require
   [amble.errors :as errors]
   [amble.models.models :refer [db-to-game-id]]
   [amble.server.server :as server]
   [re-frame.core :as re-frame]))

(defn print-db
  "Dispatches an event to print the current :game portion of the app-db."
  []
  (re-frame/dispatch [::print-db]))

(re-frame/reg-event-db
  ::print-db
  (fn [db _]
    (println (:game db))
    db))

(defn index-to-player
  "Maps an integer index to a player keyword."
  [i]
  (case i
    0 :player-one
    1 :player-two
    2 :player-three
    3 :player-four
    4 :player-five
    5 :player-six
    nil))

(defn set-player
  "Dispatches an event to set the current player in the db."
  [i]
  (re-frame/dispatch [::set-player i]))

(re-frame/reg-event-db
  ::set-player
  (fn [db [_ i]]
    (assoc-in db [:game :settings :player]
              (index-to-player i))))

(re-frame/reg-event-fx
  ::on-game-delete-failure
  (fn [_ [_ event]]
    (println "Failed to delete game:" event)
    {}))

(re-frame/reg-event-fx
  ::on-game-delete-success
  (fn [_ [_ event]]
    (println "Game deleted successfully:" event)
    {}))

(re-frame/reg-event-fx
  ::delete-game
  (fn [{:keys [db]} _]
    (let [game-id (db-to-game-id db)]
      {:dispatch [::server/delete-game game-id ::on-game-delete-success ::on-game-delete-failure]})))

(defn delete-game []
  (re-frame/dispatch [::delete-game]))

(defn throw-error
  "Throws an error to test error handling."
  []
  (let [test-error (new js/Error "Hi, I'm a test error in this world.")]
    (re-frame/dispatch [::errors/error "" test-error])))

