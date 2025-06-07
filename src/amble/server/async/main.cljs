(ns amble.server.async.main
  (:require
   [amble.server.async.server :as async-server]
   [re-frame.core :as re-frame]))

(defonce socket-atom (atom nil))

(defn websockets-url [game-id]
  ;;   (str "ws://" (environment :host :amble) ":" (environment :port :amble) "/move/" "async/" "?game-id=" game-id))
  (str "ws://" "localhost"  ":" 3000 "/move/" "async/" "?game-id=" game-id))

(defn connect-websocket! [url]
  (let [ws (js/WebSocket. url)]
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
