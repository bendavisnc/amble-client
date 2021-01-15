(ns amble-client.board-pieces
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros
   [cljs.core.async :refer [go]]))

(def classname "designatee")
(def piece-size 0.023)

(defn piece [& {:keys [x, y, size, class, index]}]
  (let [unique-key (str class
                        index)]
    [:circle {:cx     x,
              :cy     y
              :r      size
              :key    unique-key
              :id     unique-key
              :data-i index
              :class class}]))

(defn board-pieces-fn [board-piece-coordinates]
  (fn []
    (into [:g]
          (for [[i, [x, y]] (map-indexed vector board-piece-coordinates)]
            (piece :x x
                   :y y
                   :size (* 0.98 piece-size)             ;; Cheap way to prevent seeing a placeholder piece when a normal piece is sitting above.
                   :class classname
                   :index i)))))

(defmethod ig/init-key :amble/board-pieces [_ {:keys [game-id, resource-chan-fn]}]
  (go (let [board-piece-coordinates (async/<! (resource-chan-fn game-id))]
        (board-pieces-fn board-piece-coordinates))))
