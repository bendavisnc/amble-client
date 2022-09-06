(ns amble-client.board-pieces
  "Represents stationary pieces that map where player pieces can go."
  (:require [integrant.core :as ig]))

(def classname "board-piece")
(def piece-size 0.023)

(defn- piece [& {:keys [x, y, size, id, class]}]
  [:circle {:cx x,
            :cy y
            :r size
            :key id
            :id id
            :class class}])

(defn- board-pieces [app-atom]
  (fn []
    (let [pieces (:board-pieces @app-atom)]
      [:<>
       (for [{:keys [x, y, is-active?, index]} pieces]
         (piece :x x
                :y y
                :size piece-size
                :id (str classname "-" index) `:class (str classname
                                                           (if is-active? " active" ""))))])))

(defmethod ig/init-key :amble/board-pieces [_, {:keys [app-atom]}]
  (board-pieces app-atom))
