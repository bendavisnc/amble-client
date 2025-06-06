(ns amble.views.pieces.player
  (:require
   [amble.models.pieces.player :as player]
   [amble.views.pieces.piece :as piece-view]
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(def classname "player-piece")

(def event-to-coord-fn (piece-view/coord-conv))

(defn event-to-coord* [board-elem, e] 
  (event-to-coord-fn board-elem e))

(def dispatch-map
  {"mousedown" ::player/move-start
   "touchstart" ::player/move-start
   "mousemove" ::player/move-update
   "touchmove" ::player/move-update
   "mouseup"   ::player/move-end
   "touchend"  ::player/move-end})

(defn e-to-event [e]
  (let [event-to-coord (partial event-to-coord* (.getElementById js/document "board"))
        [x, y] (event-to-coord e)]
    {:x x
     :y y
     :event-type (.-type e)}))

(defn userfeedback-handler* [player-id, index, e]
  (println [player-id, index, e])
  (let [event (-> e
                  e-to-event
                  (assoc :player-id player-id)
                  (assoc :index index))
        event-type (.-type e)
        action (get dispatch-map event-type)]
    (println [action event])
    (if action
      (re-frame/dispatch [action event])
      (throw (js/Error. (str "No user feedback handler defined for " event-type))))))

(defn pieces [players]
  [:<>
   (for [player-id (keys players)
         :let [positions (get-in players [player-id :position])]]
     ^{:key player-id}
     [:<>
      (for [[index, [x, y]] (map-indexed vector positions)
            :let [userfeedback-handler (partial userfeedback-handler* player-id, index)]]
        (piece-view/piece :x x
                          :y y
                          :size piece-view/piece-size
                          :id (gstring/format "%s-%s-%s" classname (name player-id), index)
                          :class [classname, "player", (name player-id)]
                          :extra-opts {:on-mouse-down userfeedback-handler
                                       :on-mouse-up userfeedback-handler
                                       :on-mouse-move userfeedback-handler
                                       :on-touch-start userfeedback-handler
                                       :on-touch-end userfeedback-handler
                                       :on-touch-move userfeedback-handler}))])])

(comment (gstring/format "player piece %s"))
