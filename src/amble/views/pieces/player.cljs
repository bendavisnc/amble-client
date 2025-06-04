(ns amble.views.pieces.player
  (:require
   [amble.views.pieces.piece :as piece]
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(def classname "player-piece")

(def dispatch-map
  {"mousedown" ::move-start
   "touchstart" ::move-start
   "mousemove" ::move-update
   "touchmove" ::move-update
   "mouseup"   ::move-end
   "touchend"  ::move-end})

(defn userfeedback-handler [event]
  (let [event-type (.-type event)
        action (get dispatch-map event-type)]
    (if action
      (re-frame/dispatch [action event])
      (throw (js/Error. (str "No user feedback handler defined for " event-type))))))

(defn pieces [players]
  [:<>
   (for [player-id (keys players)
         :let [positions (get-in players [player-id :position])]]
     ^{:key player-id}
     [:<>
      (for [[index, [x, y]] (map-indexed vector positions)]
        (piece/piece :x x
                     :y y
                     :size piece/piece-size
                     :id (gstring/format "%s-%s-%s" classname (name player-id), index)
                     :class [classname, "player", (name player-id)]
                     :extra-opts {:on-mouse-down userfeedback-handler
                                  :on-mouse-up userfeedback-handler
                                  :on-touch-start userfeedback-handler
                                  :on-touch-end userfeedback-handler
                                  :on-touch-move userfeedback-handler}))])])

(comment (gstring/format "player piece %s"))
