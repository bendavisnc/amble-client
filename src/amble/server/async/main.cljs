(ns amble.server.async.main
  (:require
   [amble.models.pieces.player :as player-model]
   [amble.server.async.connection :as connection]
   [amble.server.async.server :as async-server]
   [goog.string.format]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
  ::async-server/initialize
  (fn [{:keys [db]} [_ game-id]]
    ;; Use the connection module to create a websocket connection.
    (connection/connect-websocket! game-id)
    nil))

(re-frame/reg-event-fx
  ::async-server/on-message
  (fn [{:keys [db]} [_ message]]
    {:db db
     :dispatch [::player-model/on-move-remote message]}))

(re-frame/reg-event-fx
  ::async-server/on-open
  (fn [_ _]))

(re-frame/reg-event-fx
  ::async-server/on-close
  (fn [{:keys [db]} _]
    (println "websocket connection closed. Reinitializing...")
    {:db db
     :dispatch [::async-server/initialize
                 (get-in db [:game :game-id])]})) 

(re-frame/reg-event-fx
  ::async-server/on-error
  (fn [_ [_ e]]
    (throw (new js/Error
                (str "WebSocket error: " (.-message e))))))
