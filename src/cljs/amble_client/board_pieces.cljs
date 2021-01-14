(ns amble-client.board-pieces
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(def classname "designatee")
(def piece-size 0.023)

(defn piece-markup [& {:keys [x, y, size, class, i]}]
  (let [unique-key (str class
                        (or i
                            [x, y]))]
    [:circle {:cx     x,
              :cy     y
              :r      size
              :key    unique-key
              :id     unique-key
              :data-i i
              :class class}]))

(defmethod ig/init-key :amble/board-pieces [_ {:keys [game-id, resource-chan-fn]}]
  (go
    (let [coordinates (:body (async/<! (resource-chan-fn game-id)))]
      (for [[i, [x, y]] (map-indexed vector coordinates)]
        (piece-markup :x x
                      :y y
                      :size (* 0.98 piece-size)             ;; Cheap way to prevent seeing a placeholder piece when a normal piece is sitting above.
                      :class classname
                      :index i)))))

