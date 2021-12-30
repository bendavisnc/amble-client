(ns amble-client.v3.piece
  (:require [amble-client.v3.global-state :as gs]))

(def piece-type-player :piece-type-player)
(def piece-type-landing :piece-type-landing)

;; todo - turn into multimethod
(defn piece-type-class [piece-type]
  (cond (= piece-type-player piece-type)
        "player"
        (= piece-type-landing piece-type)
        "landing"
        true
        (throw (new js/Error (str "No known piece type, \"" piece-type "\".")))))

(defn piece [s piece-type, index, mouse-event-fns]
  [:circle {:cx     (gs/get s index :position :x)
            :cy     (gs/get s index :position :y)
            :r      (gs/get s index :size :radius)
            :key    (str piece-type index)
            :id     (str piece-type index)
            :data-i index
            :class  (piece-type-class piece-type)
            :on-mouse-down (:on-mouse-down mouse-event-fns)
            :on-mouse-up (:on-mouse-up mouse-event-fns)}])


