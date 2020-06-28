(ns amble-client.interaction.core
  (:require [amble-client.state :as amble-client-state]
            [amble-client.utils :as utils]))

(def atom-contemporary-move (atom {}))

(def atom-event-to-coord (atom nil))

(defn on-drag-start! [& args]
  (println "on drag start")
  (if (not (:player-index (deref atom-contemporary-move)))
    (let [player-index (utils/player-index (-> args
                                               first
                                               (aget "srcElement")))]
      (assert (number? player-index)
              "Problem encountered while starting new piece drag, no index.")
      (swap! atom-contemporary-move assoc :player-index player-index))))

(defn on-drag! [e]
  (println "on drag")
  (let [player-elem (-> e
                        (aget "srcElement"))
        player-index (utils/player-index player-elem)
        ;is-correct-piece (= player-index (:player-index (deref atom-contemporary-move)))]
        is-correct-piece true]
    (if is-correct-piece
      (let [
            ;[x* (aget (first args) "x")
            ; y* (aget (first args) "y")
            ; [x, y] (map (comp
            ;                   (partial * 1)
            ;                   #(/ % 1000)
            ;             [x*, y*]
            [x, y] ((deref atom-event-to-coord)
                    e)
            player-piece-index (js/parseInt (.getAttribute player-elem "data-i"))]
        (.preventDefault e)
        (amble-client-state/update! :placement :player player-index player-piece-index 0 x)
        (amble-client-state/update! :placement :player player-index player-piece-index 1 y)
        (.log js/console (clj->js [x y]))
        (.log js/console player-elem)
        false))))

(defn on-drag-end! [& args]
  (println "on drag end"))
  ;(swap! atom-contemporary-move assoc :player-index nil))

(defn init! []
  (reset! atom-event-to-coord (utils/event-to-coord))
  (let [board-selection (.querySelector js/document "svg#board")
        player-selection (.querySelectorAll board-selection "circle.player")]
    (.forEach player-selection
              (fn [player-piece-elem]
                (.addEventListener player-piece-elem
                                   "mousedown"
                                   on-drag-start!)
                (.addEventListener player-piece-elem
                                   "mousemove"
                                   on-drag!)
                (.addEventListener player-piece-elem
                                   "mouseup"
                                   on-drag-end!)))
    (.addEventListener board-selection "mouseup" on-drag-end!)))

