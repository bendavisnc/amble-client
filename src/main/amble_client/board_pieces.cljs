(ns amble-client.board-pieces
  "Represents stationary pieces that map where player pieces can go."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.board :as board-resource])
  (:require-macros [cljs.core.async :refer [go]]))

(def classname "board-piece")
(def piece-size 0.023)

(def app-atom-chan (async/chan))

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
                :id (str classname "-" index) 
                :class (str classname
                            (if is-active? " active" ""))))])))
(go (let [app-atom (async/<! app-atom-chan)
          game-id (:game-id @app-atom)
          board-response (async/<! (board-resource/get! game-id))
          board-pieces (vec (for [[i, [x,y]] (map-indexed vector board-response)]
                              {:x x
                               :y y
                               :index i
                               :is-active? false}))]
      (swap! app-atom assoc :board-pieces board-pieces)))

(defmethod ig/init-key :amble/board-pieces [_, {:keys [app-atom, app-ready-chan]}]
  (go (async/<! app-ready-chan)
      (async/>! app-atom-chan app-atom))
  (board-pieces app-atom))
