(ns amble-client.board-pieces
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros
   [cljs.core.async :refer [go]]))

(def classname "board-pieces")
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

;; (defn board-pieces-fn [board-piece-coordinates]
;;   (fn []
;;     (into [:g]
;;           (for [[i, [x, y]] (map-indexed vector board-piece-coordinates)]
;;             (piece :x x
;;                    :y y
;;                    :size (* 0.98 piece-size)             ;; Cheap way to prevent seeing a placeholder piece when a normal piece is sitting above.
;;                    :class classname
;;                    :index i)))))

(defn init []
  [
   (piece :x 0
           :y 0
           :size piece-size 
           :class classname
           :index 0)
   (piece :x 0
          :y 0.5
          :size piece-size 
          :class classname
          :index 1)])


(defmethod ig/init-key :amble/board-pieces [_]
  (init))