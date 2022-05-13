(ns amble-client.player-pieces
  "Draws pieces on the board and captures interaction (mouse) input."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [reagent.core :as reagent]
            [amble-client.utils :as utils])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(def classname "player")
(def piece-size 0.023)

(def mouse-chans {:on-mouse-down (async/chan 1)
                  :on-mouse-up (async/chan 1)
                  :on-mouse-move (async/chan 1)})

(def draw-chan (async/chan 1))

(def coord-conv-fn-atom (atom (fn [_]
                                (throw "The function \"coord-conv\" isn't set."))))

(def piece-coordinates-atom (reagent/atom nil))
(def resource-chan-move-add-atom (reagent/atom nil))

(defn send-move! [game-id, player-id, mouse-drag-coords]
  (println "Sending move.")
  (let [move-add! (deref resource-chan-move-add-atom)
        _ (assert (not (nil? move-add!))
                  "Resource for adding moves is not set for some reason.")]
    (move-add! game-id,
               player-id,
               mouse-drag-coords)))

(defn init-mouse-chans!
  "Adds an event listener that updates a chan set in an infinite go loop.
   Inside the go loop, mouse move events between down and up events are
   accumulated and sent to `send-move!`.
   Each discreet move event will also cause the move element's coord in
   `piece-coordinate-atom` to update."
  [game-id, svg-board-elem]
  (.addEventListener svg-board-elem
                     "mousemove"
                     (fn [e]
                       (async/put! (:on-mouse-move mouse-chans)
                                   e)))
  (go-loop []
    (let [mouse-down-event (async/<! (:on-mouse-down mouse-chans))
          piece-id (-> mouse-down-event
                       (aget "target")
                       (aget "id"))
          player-id (-> mouse-down-event
                        (aget "target")
                        (.getAttribute "data-player-id"))]
      (loop [mouse-drag-coords []]
        (if (async/poll! (:on-mouse-up mouse-chans))
          (do (send-move! game-id, player-id, mouse-drag-coords)
              nil)
          ;; else
          (let [mouse-move-event (async/<! (:on-mouse-move mouse-chans))
                mouse-drag-coord ((deref coord-conv-fn-atom)
                                  mouse-move-event)]
            (async/put! draw-chan {:piece-id piece-id, :draw-coord mouse-drag-coord})
            (recur (conj mouse-drag-coords
                         mouse-drag-coord))))))
    (recur))
  nil)

(defn init-drawing! []
  (go-loop []
    (let [{:keys [piece-id, draw-coord]} (async/<! draw-chan)]
      (swap! piece-coordinates-atom
             assoc
             (keyword piece-id)
             draw-coord)
      (recur))))

(defn piece [& {:keys [x, y, size, class, id, player-id]}]
  [:circle {:cx     x
            :cy     y
            :r      size
            :key    id
            :id     id
            :data-player-id player-id
            :class class
            :on-mouse-down (fn [e]
                             (.persist e)
                             (async/put! (:on-mouse-down mouse-chans) e))
            :on-mouse-up (fn [e]
                           (async/put! (:on-mouse-up mouse-chans) e))}])

(defn piece-id [player-id, [x, y]]
  (keyword (str (name player-id)
                [x, y])))

(defn piece-coordinates [player-coordinates]
  "Converts a map with `player id` keys to a map with `piece id` keys."
  (into {}
        (mapcat identity (for [[player-id player-coordinates-seq] player-coordinates]
                           (for [[x, y] player-coordinates-seq]
                             [(piece-id player-id [x, y])
                              [x, y]])))))

(defn player-pieces-fn [player-coordinates]
  (fn []
    (into [:g]
          (vec (mapcat identity (for [[player-id player-coordinates-val] player-coordinates]
                                  (for [[x, y] player-coordinates-val]
                                    (let [piece-id* (piece-id player-id, [x, y])
                                          [x-from-atom, y-from-atom] (-> piece-coordinates-atom deref piece-id*)]
                                      (piece :x x-from-atom
                                             :y y-from-atom
                                             :size piece-size
                                             :class (str classname
                                                         " "
                                                         (name player-id))
                                             :id piece-id*
                                             :player-id player-id)))))))))

;;
(defmethod ig/init-key :amble/player-pieces [_]
  (fn [] nil))
  
