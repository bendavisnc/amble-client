(ns amble-client.game-play
  "A centralized place for defining behavior based on user feedback events."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

;; This is just a var to have a reference to the real reagent/react atom, via di
(def app-atom-holder (atom nil)) 

(def piece-grab-chan (async/chan))
(def piece-release-chan (async/chan))
(def piece-move-chan (async/chan))

(defn on-move! [player-id, player-piece-index, x, y]
  (println [player-id, player-piece-index, x, y])
  (let [app-atom (deref app-atom-holder)]
    (swap! app-atom assoc-in [:player-pieces player-id player-piece-index 0] x)
    (swap! app-atom assoc-in [:player-pieces player-id player-piece-index 1] y)))

(defn on-move-finally! [player-id, moves]
  (println (str "Player move ready, \"" moves "\"."))
  (println "  (" player-id")"))

(defn- element-to-player-id [element]
  (keyword (.getAttribute element
                          "data-player-id")))

(defn- element-to-piece-index [element]
  (js/parseInt (.getAttribute element
                              "data-player-piece-index")))
 

(defn coordinate-converter* []
  (utils/coord-conv
   (.getElementById js/document "board")))

(def coordinate-converter
  (memoize coordinate-converter*))
  
(go-loop []
  (let [piece-grab-event (async/<! piece-grab-chan)
        player-id (element-to-player-id (.-target piece-grab-event))
        player-piece-index (element-to-piece-index (.-target piece-grab-event))]
    (loop [moves-acc []]
      (if (async/poll! piece-release-chan)
        (on-move-finally! nil moves-acc)
        (let [move (async/<! piece-move-chan)
              [x, y] ((coordinate-converter)
                      move)]              
          (on-move! player-id, player-piece-index, x, y)
          (recur (conj moves-acc move)))))
    (recur)))


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


(defmethod ig/init-key :amble/game-play [_ {:keys [app-atom]}]
  (reset! app-atom-holder app-atom)
  {:handle-ui-event handle-ui-event})


