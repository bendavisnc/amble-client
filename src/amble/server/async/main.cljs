(ns amble.server.async.main
  (:require
   [amble.config :as amble-config]
   [amble.models.pieces.player :as player-model]
   [amble.server.async.server :as async-server]
   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :as re-frame]))

(defonce socket-atom (atom nil))

(defn websockets-url [game-id]
  (gstring/format "ws://%s:%d/move/async/?game-id=%s"
                  amble-config/SERVER_HOST
                  amble-config/SERVER_PORT
                  (name game-id)))

(defn connect-websocket! [url]
  (let [ws (js/WebSocket. url)]
    (.dir js/console ws)
    (set! (.-onmessage ws) #(re-frame/dispatch [::async-server/on-message (.-data %)]))
    (set! (.-onopen ws) #(re-frame/dispatch [::async-server/on-open]))
    (set! (.-onclose ws) #(re-frame/dispatch [::async-server/on-close]))
    (set! (.-onerror ws) #(re-frame/dispatch [::async-server/on-error %]))
    (reset! socket-atom ws)))

(re-frame/reg-fx
  ::initialize
  (fn [game-id]
    (let [url (websockets-url game-id)]
      (connect-websocket! url))))

(re-frame/reg-event-fx
  ::async-server/initialize
  (fn [{:keys [db]} [_ game-id]]
    {:db db
     ::initialize game-id}))

(re-frame/reg-event-fx
  ::async-server/on-open
  (fn [_ _]))

(re-frame/reg-event-fx
  ::async-server/on-close
  (fn [_ _]))

(re-frame/reg-event-fx
  ::async-server/on-message
  (fn [{:keys [db]} [_ message]]
    {:db db
     :dispatch [::player-model/on-move-remote message]}))

(re-frame/reg-event-fx
  ::async-server/on-error
  (fn [_ [_ e]]
    (throw (new js/Error
                (str "WebSocket error: " (.-message e))))))
