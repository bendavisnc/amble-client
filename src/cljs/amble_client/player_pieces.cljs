(ns amble-client.player-pieces
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(def classname "player")
(def piece-size 0.023)

(defn piece [& {:keys [x, y, size, class, i]}]
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

(defmethod ig/init-key :amble/player-pieces [_ {:keys [game-id, resource-chan-fn]}]
  (go
    (let [players (async/<! (resource-chan-fn game-id))
          player-coordinates (async/<! (async/into {}
                                                   (async/merge
                                                    (for [player-id players]
                                                      (async/pipe (resource-chan-fn game-id (name player-id))
                                                                  (async/chan 1
                                                                              (map (fn [response]
                                                                                     [player-id response]))))))))]

      (for [[player-name coordinates] player-coordinates]
        (for [[i, [x, y]] (map-indexed vector coordinates)]
          (piece :x x
                 :y y
                 :size piece-size
                 :class (str classname
                             " "
                             player-name)
                 :index i))))))
