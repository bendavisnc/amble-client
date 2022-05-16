(ns amble-client.game-play
  "A centralized place for defining behavior based on user feedback events."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))


(def piece-grab-chan (async/chan))
(def piece-release-chan (async/chan))
(def piece-move-chan (async/chan))
;; This is just used instead of some other mutable var solution.
(def app-atom-chan (async/chan))

(defn on-move! [app-atom, player-id, player-piece-index, x, y]
  (swap! app-atom assoc-in [:player-pieces player-id player-piece-index] [x, y]))

(defn on-move-finally! [player-id, moves]
  (println (str "Player move ready, \"" moves "\"."))
  (println "  (" player-id")"))

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
  
(go-loop [app-atom (async/<! app-atom-chan)
          event-to-coord (utils/coord-conv
                          (.getElementById js/document "board"))]
  (let [piece-grab-event (async/<! piece-grab-chan)
        player-id (element-to-player-id (.-target piece-grab-event))
        player-piece-index (element-to-piece-index (.-target piece-grab-event))]
    (loop [moves-acc []]
      (if (async/poll! piece-release-chan)
        (on-move-finally! nil moves-acc)
        (let [move (async/<! piece-move-chan)
              [x, y] (event-to-coord
                      move)]              
          (on-move! app-atom player-id, player-piece-index, x, y)
          (recur (conj moves-acc [x, y])))))
    (recur app-atom
           event-to-coord))) 

(defmethod ig/init-key :amble/game-play [_ {:keys [app-atom]}]
  (async/put! app-atom-chan app-atom)
  {:handle-ui-event handle-ui-event}) 


