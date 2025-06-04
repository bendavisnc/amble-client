(ns amble.views.pieces.player
  (:require
   [amble.views.pieces.piece :as piece]
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(def classname "player-piece")

(defn userfeedback-handler [event]
  (cond (= "mousedown"
           (.-type event))
        (re-frame/dispatch [::move-start event])
        (= "mousemove"
           (.-type event))
        (re-frame/dispatch [::move-update event])
        (= "mouseup"
           (.-type event))
        (re-frame/dispatch [::move-end event])
        (= "mousemove"
           (.-type event))
        (re-frame/dispatch [::move-update event])
        (= "touchstart"
           (.-type event))
        (re-frame/dispatch [::move-start event])
        (= "touchmove"
           (.-type event))
        (re-frame/dispatch [::move-update event])
        (= "touchend"
           (.-type event))
        (re-frame/dispatch [::move-end event])
        :else
          (throw (new js/Error
                      (gstring/format "No user feedback handler defined for %s" (.-type event)))))) 

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
