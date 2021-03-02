(ns amble-client.remote-control
  "Encompasses reaching out to server and reactively updating board and piece state accordingly."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [haslett.client :as haslett-client]
            [haslett.format :as haslett-format]
            [amble-client.resource.environment :refer [environment]]
            [clojure.spec.alpha :as spec]
            [clojure.edn :as edn])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(defn websockets-url [game-id]
  (str "ws://" (environment :host :amble) ":" (environment :port :amble) "/game/" "async/" "?game-id=" game-id))

;; Devnote, I want this to be the key `:resource-type`.
;(defmulti amble-async-request first)
(defmulti amble-async-request (fn [& args]
                                (first args)))

(defmethod amble-async-request :move [_, game-id, player-id, move-id]
  (println "todo, this thing")
  (println [game-id, player-id, move-id]))
;
(spec/def ::resource
  (spec/cat :game-id string?, :player-id string?, :id string?))

(spec/def ::resource-type #{:move})

(spec/def ::async-message (spec/map-of ::resource-type ::resource))

(defn message-deconstructed [message]
  (spec/conform ::async-message message))

(defmethod ig/init-key :amble/remote-control [_ {:keys [game-id]}]
  (let [websockets-connection-chan
        (haslett-client/connect (websockets-url game-id))]
    (go-loop []
      (let [{:keys [source] :as connection-stream}
            (async/<! websockets-connection-chan)
            async-message (async/<! source)
            _ (assert (not (nil? async-message)))
            message-parsed (edn/read-string async-message)
            _ (assert (map? message-parsed))
            message (message-deconstructed message-parsed)
            resource-type (first (keys message))
            {:keys [game-id, player-id, id]} (message resource-type)]
        (println "Received new async resource message.")
        (println (str "  "
                      [async-message
                       resource-type
                       message]))
        (amble-async-request resource-type, game-id, player-id, id)
        (recur)))))
