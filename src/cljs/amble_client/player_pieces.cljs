(ns amble-client.player-pieces
  "Represents player pieces that can change position based on user feedback."
  (:require [integrant.core :as ig]
            [reagent.core :as reagent]
            [amble-client.utils :as utils]))

(def classname "player")
(def piece-size 0.023)

(defn unique-player-key [player-id index]
  (str (name player-id)
       "-"
       "piece"
       "-"
       index))

(defn- piece [& {:keys [x, y, size, class, index, player-id, unique-key, user-feedback-handler]}]
  [:circle {:cx     x,
            :cy     y
            :r      size
            :key    unique-key
            :id     unique-key
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

(defn- player-pieces [supplier, game-play]
  (fn []
    (let [player-pieces (supplier)]
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
                   :player-id player-id
                   :unique-key unique-key 
                   :user-feedback-handler (:handle-ui-event game-play)))])])))


(defmethod ig/init-key :amble/player-pieces [_, {:keys [supplier, game-play]}]
  (player-pieces supplier game-play))