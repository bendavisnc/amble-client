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

(defn- piece [& {:keys [x, y, size, class, index, unique-key]}]
  [:circle {:cx     x,
            :cy     y
            :r      size
            :key    unique-key
            :id     unique-key
            :class class}])

(defn- player-pieces [state-handler]
  (fn []
    (let [player-pieces (state-handler)]
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
                   :unique-key unique-key))])]))) 


(defmethod ig/init-key :amble/player-pieces [_, {:keys [state-handler]}]
  (player-pieces state-handler))