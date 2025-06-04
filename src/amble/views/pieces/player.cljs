(ns amble.views.pieces.player
  (:require
   [amble.models.pieces.player :as player]
   [amble.views.pieces.piece :as piece-view]
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(def classname "player-piece")

(def dispatch-map
  {"mousedown" ::player/move-start
   "touchstart" ::player/move-start
   "mousemove" ::player/move-update
   "touchmove" ::player/move-update
   "mouseup"   ::player/move-end
   "touchend"  ::player/move-end})

(defn e-to-event [e]
  {:x (.-clientX e)
   :y (.-clientY e)
   :event-type (.-type e)})

(defn userfeedback-handler* [player-id, e]
  (let [event (assoc (e-to-event e)
                     :player player-id)
        event-type (.-type e)
        action (get dispatch-map event-type)]
    (if action
      (re-frame/dispatch [action event])
      (throw (js/Error. (str "No user feedback handler defined for " event-type))))))

(defn pieces [players]
  [:<>
   (for [player-id (keys players)
         :let [positions (get-in players [player-id :position])
               userfeedback-handler (partial userfeedback-handler* player-id)]]
     ^{:key player-id}
     [:<>
      (for [[index, [x, y]] (map-indexed vector positions)]
        (piece-view/piece :x x
                          :y y
                          :size piece-view/piece-size
                          :id (gstring/format "%s-%s-%s" classname (name player-id), index)
                          :class [classname, "player", (name player-id)]
                          :extra-opts {:on-mouse-down userfeedback-handler
                                       :on-mouse-up userfeedback-handler
                                       :on-touch-start userfeedback-handler
                                       :on-touch-end userfeedback-handler
                                       :on-touch-move userfeedback-handler}))])])

(comment (gstring/format "player piece %s"))
