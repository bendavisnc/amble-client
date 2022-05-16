(ns amble-client.board-pieces
  "Represents stationary pieces that map where player pieces can go."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros
   [cljs.core.async :refer [go]]))

(def classname "board-piece")
(def piece-size 0.023)

(defn- piece [& {:keys [x, y, size, class, index]}]
  (let [unique-key (str class
                        "-"
                        index)]
    [:circle {:cx     x,
              :cy     y
              :r      size
              :key    unique-key
              :id     unique-key
              :class class}]))

(defn- board-pieces [state-handler]
  (fn []
    (let [pieces (state-handler)]
      [:<>
       (for [i (range (count pieces))
             :let [p (pieces i)]]
         (piece :x (p 0)
                :y (p 1)
                :size piece-size
                :class classname
                :index i))])))


(defmethod ig/init-key :amble/board-pieces [_, {:keys [state-handler]}]
  (board-pieces state-handler))