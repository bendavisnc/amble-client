(ns amble.server.async.connection
  (:require
   [amble.config :as amble-config]
   [amble.server.async.server :as async-server]
   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :as re-frame]))

(defonce socket-atom (atom nil))

(defn websockets-url [game-id]
  (when (= "localhost" amble-config/SERVER_HOST)
    (println "Using `localhost` for websocket url."))
  (let [ws-protocol (if (= "https:" 
                           (.-protocol (.-location js/window)))
                       "wss"
                       "ws")]
    (gstring/format "%s://%s/move/async/?game-id=%s"
                    ws-protocol
                    amble-config/SERVER_HOST
                    (name game-id))))

(defn connect-websocket! [game-id]
  (let [url (websockets-url game-id)
        _ (println (gstring/format "Connecting to websocket connection at `%s`." url))
        ws (js/WebSocket. url)]
    (set! (.-onmessage ws) #(re-frame/dispatch [::async-server/on-message (.-data %)]))
    (set! (.-onopen ws) #(re-frame/dispatch [::async-server/on-open]))
    (set! (.-onclose ws) #(re-frame/dispatch [::async-server/on-close]))
    (set! (.-onerror ws) #(re-frame/dispatch [::async-server/on-error %]))
    (reset! socket-atom ws)))
