(ns amble-client.v3.piece
  (:require [amble-client.v3.global-state :as gs]
            [amble-client.utils :as utils]))

(def piece-type-player :piece-type-player)
(def piece-type-landing :piece-type-landing)

(defn is-landing-piece? [piece-type]
  (= piece-type-landing piece-type))

(defn landing-piece [s index, mouse-event-fns]
  (let [game-id (gs/get s :current)
        d (nth (gs/get s game-id :pieces :landing)
               index)
        {:keys [x, y]} (:position d)
        {:keys [radius]} (:size d)]
    [:circle {:cx     x
              :cy     y
              :r      radius
              :key    (str :landing "-" index)
              :id    (str :landing "-" index)
              :data-i index
              :class "landing"
              :on-mouse-down (:on-mouse-down mouse-event-fns)
              :on-mouse-up (:on-mouse-up mouse-event-fns)}]))

(defn player-piece [s player-id, index, mouse-event-fns]
  (let [game-id (gs/get s :current)
        player-id-name (name player-id)
        d (nth (gs/get s game-id :pieces :player player-id)
               index)
        _ (println "wut")
        _ (println d)
        {:keys [x, y]} (:position d)
        {:keys [radius]} (:size d)]
    [:circle {:cx     x
              :cy     y
              :r      radius
              :key    (str :player "-" player-id-name "-" index)
              :id    (str :player "-" player-id-name "-" index)
              :data-i index
              :class (str "player" " " player-id-name)
              :on-mouse-down (:on-mouse-down mouse-event-fns)
              :on-mouse-up (:on-mouse-up mouse-event-fns)}]))

