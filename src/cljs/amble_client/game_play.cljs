(ns amble-client.game-play
  "Producer of move events."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))


;; channels, input
(def piece-grab-chan (async/chan))
(def piece-release-chan (async/chan))
(def piece-move-chan (async/chan))
(def board-piece-closest-chan (async/chan))

;; channels, output
(def move-remote-chan (async/chan))
(def move-local-chan (async/chan))
(def move-xy-chan (async/chan))

(defn- element-to-game-id [element]
  (keyword (.getAttribute element
                          "data-game-id")))

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
  
(def event-to-coord-cached
  (memoize (fn []
             (utils/coord-conv
               (.getElementById js/document "board")))))
;; A go loop for turning mouse dragging behavior into move events.
;; Invokes out to the chans that are provided as dependencies.
(go-loop [board-piece-closest (async/<! board-piece-closest-chan)]
  (println "Waiting for game play.")
  (let [piece-grab-event (async/<! piece-grab-chan)
        game-id (element-to-game-id (.-target piece-grab-event))
        player-id (element-to-player-id (.-target piece-grab-event))
        player-piece-index (element-to-piece-index (.-target piece-grab-event))
        event-to-coord (event-to-coord-cached)]
    (loop [moves []]
      (if (async/poll! piece-release-chan)
        (let [_ (println "Local move complete!")
              [last-x, last-y] (last moves)
              [x, y] (board-piece-closest last-x, last-y)]
          (async/>! move-local-chan {:game-id game-id 
                                     :player-id player-id 
                                     :player-piece-index player-piece-index
                                     :move moves
                                     :x x
                                     :y y
                                     :origin :local}))
        (let [move (async/<! piece-move-chan)
              [x, y] (event-to-coord
                      move)]              
          (async/>! move-xy-chan {:player-id player-id 
                                  :player-piece-index player-piece-index
                                  :x x
                                  :y y})
          (recur (conj moves [x, y]))))))
  (recur board-piece-closest))

(defmethod ig/init-key :amble/game-play [_ {:keys [move-local-chan, move-xy-chan, board-piece-closest]}]
  (async/pipe amble-client.game-play/move-local-chan move-local-chan)
  (async/pipe  amble-client.game-play/move-xy-chan move-xy-chan)
  (async/put! board-piece-closest-chan board-piece-closest)
  {:handle-ui-event handle-ui-event}) 


