(ns amble-client.async-resource.base
  (:require [amble-client.resource.environment :refer [environment]]
            [cljs.spec.alpha :as spec]
            [cljs.core.async :as async]
            [integrant.core :as ig]
            [haslett.client :as haslett-client]
            [clojure.edn :as edn])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(defn websockets-url [game-id]
  (str "ws://" (environment :host :amble) ":" (environment :port :amble) "/game/" "async/" "?game-id=" game-id))
;; Devnote, I want this to be the key `:resource-type`.
;(defmulti amble-async-request first)
(defmulti amble-async-request-response (fn [& args]
                                         (first args)))
(spec/def ::resource
  (spec/cat :game-id string?, :player-id string?, :id string?))

(spec/def ::resource-type #{:move})

(spec/def ::async-message (spec/map-of ::resource-type ::resource))

(defn message-deconstructed [message]
  (spec/conform ::async-message message))

(defmethod ig/init-key :amble/async-resource-base [_ {:keys [game-id]}]
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
        (amble-async-request-response resource-type, game-id, player-id, id)
        (recur)))))
