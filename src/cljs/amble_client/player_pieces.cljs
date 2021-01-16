(ns amble-client.player-pieces
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [reagent.core :as reagent]
            [amble-client.utils :as utils])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(def classname "player")
(def piece-size 0.023)

(def mouse-chans {:on-mouse-down (async/chan)
                  :on-mouse-up (async/chan)
                  :on-mouse-move (async/chan)})

(def coord-conv-fn-atom (atom (fn [_]
                                (throw "The function \"coord-conv\" isn't set."))))

(def piece-coordinates-atom (reagent/atom nil))

(defn send-move! [mouse-drag-coords]
  (println "Sending move.")
  (println mouse-drag-coords))

(defn init-mouse-chans! []
  (go-loop []
    (let [mouse-down-event (async/<! (:on-mouse-down mouse-chans))
          piece-id (-> mouse-down-event
                       (aget "target")
                       (aget "id"))]
      (loop [mouse-drag-coords []]
        (if (async/poll! (:on-mouse-up mouse-chans))
          (do (send-move! mouse-drag-coords)
              nil)
          (let [mouse-move-event (async/<! (:on-mouse-move mouse-chans))
                mouse-drag-coord ((deref coord-conv-fn-atom)
                                  mouse-move-event)]
            (swap! piece-coordinates-atom
                   assoc
                   (keyword piece-id)
                   mouse-drag-coord)
            (recur (conj mouse-drag-coords
                         mouse-drag-coord))))))
    (recur))
  nil)

(defn piece [& {:keys [x, y, size, class, id]}]
  [:circle {:cx     x
            :cy     y
            :r      size
            :key    id
            :id     id
            :class class
            :on-mouse-down (fn [e]
                             (.persist e)
                             (async/put! (:on-mouse-down mouse-chans) e))
            :on-mouse-up (fn [e]
                           (async/put! (:on-mouse-up mouse-chans) e))
            :on-mouse-move (fn [e]
                             (.persist e)
                             (async/offer! (:on-mouse-move mouse-chans)
                                           e))}])

(defn piece-id [player-id, [x, y]]
  (keyword (str (name player-id)
                [x, y])))

(defn piece-coordinates [player-coordinates]
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
                                             :id piece-id*)))))))))

(defmethod ig/init-key :amble/player-pieces [_ {:keys [game-id, resource-chan-fn]}]
  (go
    (let [players (async/<! (resource-chan-fn game-id))
          player-coordinates (async/<! (async/into {}
                                                   (async/merge
                                                    (for [player-id players]
                                                      (async/pipe (resource-chan-fn game-id player-id)
                                                                  (async/chan 1
                                                                              (map (fn [coordinates]
                                                                                     [(keyword player-id) coordinates]))))))))
          piece-coordinates (piece-coordinates player-coordinates)]
      (js/setTimeout (fn [_] ;; devnote - This is a little sad and hacky. Maybe come back to.
                       (reset! coord-conv-fn-atom (utils/coord-conv (.querySelector js/document "svg#board"))))
                     100)
      (reset! piece-coordinates-atom piece-coordinates)
      (init-mouse-chans!)
      (player-pieces-fn player-coordinates))))

