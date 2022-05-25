(ns amble-client.game-play
  "A centralized place for defining behavior based on local user feedback events."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))


(def piece-grab-chan (async/chan))
(def piece-release-chan (async/chan))
(def piece-move-chan (async/chan))
;; This is just used instead of some other mutable var solution.
(def on-move!-chan (async/chan))

;; (defn on-move! [app-atom, player-id, player-piece-index, x, y]
  ;; (swap! app-atom assoc-in [:player-pieces player-id player-piece-index] [x, y]))

(defn- element-to-player-id [element]
  (keyword (.getAttribute element
                          "data-player-id")))

(defn- element-to-piece-index [element]
  (js/parseInt (.getAttribute element
                              "data-player-piece-index")))
 
(defn handle-ui-event [e]
  (cond (= "mousedown"
           (.-type e))
        (async/put! piece-grab-chan e)
        (= "mouseup"
           (.-type e))
        (async/put! piece-release-chan e)
        (= "mousemove"
           (.-type e))
        (async/put! piece-move-chan e)
        true
        (do
          (println "User event not handled!")
          (.log js/console e))))
  
;; A go loop for turning mouse dragging behavior into move events.
(go-loop [on-move! (async/<! on-move!-chan)
          event-to-coord (utils/coord-conv
                          (.getElementById js/document "board"))]
  (let [piece-grab-event (async/<! piece-grab-chan)
        player-id (element-to-player-id (.-target piece-grab-event))
        player-piece-index (element-to-piece-index (.-target piece-grab-event))]
    (loop [moves []]
      (if (async/poll! piece-release-chan)
        (do (println (str "Sending move to server, " [player-id, player-piece-index, moves]))
            (on-move! player-id player-piece-index moves))
        (let [move (async/<! piece-move-chan)
              [x, y] (event-to-coord
                      move)]              
          (on-move! player-id, player-piece-index, x, y)
          (recur (conj moves [x, y])))))
    (recur on-move!
           event-to-coord))) 

(defmethod ig/init-key :amble/game-play [_ {:keys [on-move!]}]
  (async/put! on-move!-chan on-move!)
  {:handle-ui-event handle-ui-event}) 


