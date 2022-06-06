(ns amble-client.game-play
  "A centralized place for defining behavior based on local user feedback events."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))


(def piece-grab-chan (async/chan))
(def piece-release-chan (async/chan))
(def piece-move-chan (async/chan))

(def move-chan (async/chan))
(def move-xy-chan (async/chan))

(def event-to-coord-chan (async/chan))
;; (defn on-move! [app-atom, player-id, player-piece-index, x, y]
  ;; (swap! app-atom assoc-in [:player-pieces player-id player-piece-index] [x, y]))

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
  
;; A go loop for turning mouse dragging behavior into move events.
;; Invokes out to the chans that are provided as dependencies.
(go-loop [
          event-to-coord (async/<! event-to-coord-chan)]
  (println "Waiting for game play.")
  (let [piece-grab-event (async/<! piece-grab-chan)
        game-id (element-to-game-id (.-target piece-grab-event))
        player-id (element-to-player-id (.-target piece-grab-event))
        player-piece-index (element-to-piece-index (.-target piece-grab-event))]
    (loop [moves []]
      (if (async/poll! piece-release-chan)
        (do (println "Local move complete!")
            (async/>! move-chan {:game-id game-id 
                                 :player-id player-id 
                                 :player-piece-index player-piece-index
                                 :move moves}))
        (let [move (async/<! piece-move-chan)
              [x, y] (event-to-coord
                      move)]              
          (async/>! move-xy-chan {:player-id player-id 
                                  :player-piece-index player-piece-index
                                  :x x
                                  :y y})
          (recur (conj moves [x, y]))))))
  (recur 
         
         event-to-coord)) 

(defmethod ig/init-key :amble/game-play [_ {:keys [move-chan, move-xy-chan]}]
  (async/pipe move-chan amble-client.game-play/move-chan)
  (async/pipe move-xy-chan amble-client.game-play/move-xy-chan)
  (async/put! event-to-coord-chan (utils/coord-conv
                                    (.getElementById js/document "board")))
  {:handle-ui-event handle-ui-event}) 


