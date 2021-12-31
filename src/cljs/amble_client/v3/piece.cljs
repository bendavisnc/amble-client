(ns amble-client.v3.piece
  (:require [amble-client.v3.global-state :as gs]))

(def piece-type-player :piece-type-player)
(def piece-type-landing :piece-type-landing)

(defmulti piece-type-class (fn [x] x))
(defmethod piece-type-class piece-type-player [_] "player")
(defmethod piece-type-class piece-type-landing [_] "landing")
(defmulti piece-type-key (fn [x] x))
(defmethod piece-type-key piece-type-player [_] :player)
(defmethod piece-type-key piece-type-landing [_] :landing)




(defn piece [s piece-type, index, mouse-event-fns]
  (let [game-id (gs/get s :current)]
    [:circle {:cx     (gs/get s game-id :pieces (piece-type-key piece-type) index :position :x)
              :cy     (gs/get s game-id :pieces (piece-type-key piece-type) index :position :y)
              :r      0.023
              :key    (str piece-type index)
              :id     (str piece-type index)
              :what "wut"
              :data-i index
              :class  (piece-type-class piece-type)
              :on-mouse-down (:on-mouse-down mouse-event-fns)
              :on-mouse-up (:on-mouse-up mouse-event-fns)}]))


