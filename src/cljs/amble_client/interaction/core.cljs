(ns amble-client.interaction.core
  (:require [amble-client.state :as amble-client-state]
            [amble-client.utils :as utils]
            [amble-client.resource.move :as move-resource]))

(def atom-contemporary-move (atom nil))

(def atom-event-to-coord (atom nil))

(defn on-drag-start! [e]
  (println "on drag start")
  (if (not (:player-being-dragged (deref atom-contemporary-move)))
    (let [player-elem (aget e
                            "srcElement")]
      (assert player-elem)
      (swap! atom-contemporary-move assoc :player-being-dragged player-elem))))

(defn update-contemporary-move! [[x, y]]
  (swap! atom-contemporary-move update :move #(conj % [x, y])))

(defn on-drag! [e]
  (let [
        player-elem ((deref atom-contemporary-move)
                     :player-being-dragged)]
    (if player-elem
      (do
        (.preventDefault e)
        ;(println "on drag")
        (let [
              player-index (utils/player-index player-elem)
              ;[x* (aget (first args) "x")
              ; y* (aget (first args) "y")
              ; [x, y] (map (comp
              ;                   (partial * 1)
              ;                   #(/ % 1000)
              ;             [x*, y*]
              [x, y] ((deref atom-event-to-coord)
                      e)
              player-piece-index (js/parseInt (.getAttribute player-elem "data-i"))]
          (amble-client-state/update! :placement :player player-index player-piece-index 0 x)
          (amble-client-state/update! :placement :player player-index player-piece-index 1 y)
          (update-contemporary-move! [x, y])
          ;(.log js/console (clj->js [x y]))
          ;(.log js/console player-elem)
          true)))))

(defn on-drag-end! [& _]
  (println "on drag end")
  (let [{:keys [game-id, move]} (deref atom-contemporary-move)
        _ (println "hi")
        _ (println game-id)
        _ (println move)]
        ; move-add-promise (move-resource/add! :game-id game-id
        ;                                      :move move)
        ; on-successful-response (fn [move-response]
        ;                          (println "Posted successful move.")
        ;                          (println move-response)
        ;                          (swap! atom-contemporary-move assoc :player-being-dragged nil))
                                 
    (.log js/console "whatevs")
    (swap! atom-contemporary-move assoc :player-being-dragged nil)))


    ; (-> move-add-promise
    ;     (.then on-successful-response)
    ;     (.catch (fn [err]
    ;               (amble-client-state/update! :errors [err])
    ;               (throw err)))))




(defn atom-contemporary-move-init! []
  (reset! atom-contemporary-move
          {:game-id (or (amble-client-state/get :game-id)
                        (throw (new js/Error "No game id set.")))
           :move    []}))

(defn init! []
  (println "dunzo (start, interaction)") 
  (atom-contemporary-move-init!)
  (let [board-elem (.querySelector js/document "svg#board")
        _ (assert board-elem)
        _ (reset! atom-event-to-coord (utils/coord-conv board-elem))
        player-selection (.querySelectorAll board-elem "circle.player")
        _ (assert (< 0 (.-length player-selection)))]
        
    (.forEach player-selection
              (fn [player-piece-elem]
                (.addEventListener player-piece-elem
                                   "mousedown"
                                   on-drag-start!)
                (.addEventListener board-elem
                                   "mousemove"
                                   on-drag!)
                (.addEventListener player-piece-elem
                                   "mouseup"
                                   on-drag-end!)
                (.addEventListener player-piece-elem
                                   "touchstart"
                                   on-drag-start!)
                (.addEventListener board-elem
                                   "touchmove"
                                   on-drag!)))
                ;(.addEventListener player-piece-elem
                ;                   "touchend"
                ;                   on-drag-end!)))
    (.addEventListener board-elem "mouseup" on-drag-end!)
    (.addEventListener board-elem "touchend" on-drag-end!)))

