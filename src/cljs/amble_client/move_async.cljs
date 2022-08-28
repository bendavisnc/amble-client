(ns amble-client.move-async
  "Abstracts websockets interaction for receiving move async updates from server."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.environment :refer [environment]]
            [haslett.client :as haslett-client])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))
(def latest-move-index-chan (async/chan))

(defn websockets-url [game-id]
  (str "ws://" (environment :host :amble) ":" (environment :port :amble) "/move/" "async/" "?game-id=" game-id))

(go-loop [game-id (:game-id (deref (async/<! app-atom-chan)))
          websockets-connection-chan (haslett-client/connect (websockets-url game-id))
          move-source-chan (:source (async/<! websockets-connection-chan))]
  (assert (not (nil? game-id))
          "game-id is nil.")
  (let [move-from-server (async/<! move-source-chan)]
    (assert (not (nil? move-from-server))
            "Move from server is nil.")
    (println (str "Received new move from server, " move-from-server "."))
    (async/>! latest-move-index-chan move-from-server))
  (recur game-id
         websockets-connection-chan
         move-source-chan))

(defmethod ig/init-key :amble/move-async [_ {:keys [app-atom, latest-move-index-chan, app-ready-chan]}]
  (async/pipe amble-client.move-async/latest-move-index-chan latest-move-index-chan)
  (go
    (async/<! app-ready-chan)
    (async/>! app-atom-chan app-atom))
  nil)


