(ns amble-client.player-pieces
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [reagent.core :as reagent])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(def classname "player")
(def piece-size 0.023)

(defn piece [& {:keys [x, y, size, class, id, index]}]
  [:circle {:cx     x,
            :cy     y
            :r      size
            :key    id
            :id     id
            :data-i index
            :class class}])

(def player-coordinates-atom (reagent/atom nil))

(defn player-pieces-fn [player-coordinates]
  (fn []
    (into [:g]
          (mapcat identity
                  (for [[player-name coordinates] player-coordinates]
                    (for [[i, [x, y]] (map-indexed vector coordinates)]
                      (piece :x (nth (nth ((deref player-coordinates-atom) ;; todo - cleanup
                                           player-name)
                                          i)
                                     0)
                             :y (nth (nth ((deref player-coordinates-atom)
                                           player-name)
                                          i)
                                     1)
                             :size piece-size
                             :class (str classname
                                         " "
                                         player-name)
                             :id (str player-name
                                      [x, y])
                             :index i)))))))

(defmethod ig/init-key :amble/player-pieces [_ {:keys [game-id, resource-chan-fn]}]
  (go
    (let [players (async/<! (resource-chan-fn game-id))
          player-coordinates (async/<! (async/into {}
                                                   (async/merge
                                                    (for [player-id players]
                                                      (async/pipe (resource-chan-fn game-id (name player-id))
                                                                  (async/chan 1
                                                                              (map (fn [coordinates]
                                                                                     [player-id coordinates]))))))))]
      (reset! player-coordinates-atom player-coordinates)
      (player-pieces-fn player-coordinates))))

