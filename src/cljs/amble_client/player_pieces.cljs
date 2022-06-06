(ns amble-client.player-pieces
  "Represents player pieces that can change position based on user feedback."
  (:require [integrant.core :as ig]
            [reagent.core :as reagent]
            [cljs.core.async :as async]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def classname "player")
(def piece-size 0.023)

(def app-atom-chan (async/chan))
(def move-chan-chan (async/chan))
(def move-xy-chan-chan (async/chan))

(defn unique-player-key [player-id index]
  (str (name player-id)
       "-"
       "piece"
       "-"
       index))

(defn- piece [& {:keys [x, y, size, class, index, game-id, player-id, unique-key, user-feedback-handler]}]
  [:circle {:cx     x,
            :cy     y
            :r      size
            :key    unique-key
            :id     unique-key
            :data-game-id game-id
            :data-player-id player-id
            :data-player-piece-index index
            :class class
            :on-mouse-down (fn [e] 
                             (.persist e)
                             (.preventDefault e)
                             (user-feedback-handler e)) 
            :on-mouse-up (fn [e] 
                           (.persist e)
                           (.preventDefault e)
                           (user-feedback-handler e))}]) 

(defn- player-pieces [app-atom, game-play]
  (fn []
    (let [
          game-id (:game-id @app-atom)
          player-pieces (:player-pieces @app-atom)]
      [:<>
       (for [player-id (keys player-pieces)
             :let [pieces (player-id player-pieces)]]
         ^{:key player-id}
         [:<>
          (for [i (range (count pieces))
                :let [p (pieces i)
                      unique-key (unique-player-key player-id i)]]
            (piece :x (p 0)
                   :y (p 1)
                   :size piece-size
                   :class (str classname " " (name player-id))
                   :index i
                   :game-id game-id
                   :player-id player-id
                   :unique-key unique-key 
                   :user-feedback-handler (:handle-ui-event game-play)))])])))

;; Pulls from xy moves and updates position state from the app atom.
(go-loop [app-atom (async/<! app-atom-chan)
          move-xy-chan (async/<! move-xy-chan-chan)]
  (let [{:keys [player-id, player-piece-index, x, y]} (async/<! move-xy-chan)]
    (swap! app-atom assoc-in [:player-pieces player-id player-piece-index] [x, y]))
  (recur app-atom
         move-xy-chan))


(go-loop [
          move-chan (async/<! move-chan-chan)]
  (let [m (async/<! move-chan)]
    (println "neat")
    (println m))
  (recur move-chan))

(defmethod ig/init-key :amble/player-pieces [_, {:keys [app-atom, move-chan, move-xy-chan, game-play]}]
  (async/put! app-atom-chan app-atom)
  (async/put! move-xy-chan-chan move-xy-chan)
  (async/put! move-chan-chan move-chan)
  (player-pieces app-atom game-play))
