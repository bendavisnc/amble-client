(ns amble.views.pieces.player
  (:require
   [amble.models.pieces.player :as player]
   [amble.views.pieces.piece :as piece]
   [goog.string :as gstring]))

(def classname "player-piece")

(def dispatch-map
  {"mousedown" ::player/move-start
   "touchstart" ::player/move-start
   ;;  "mousemove" ::player/move-update
   ;;  "touchmove" ::player/move-update
   "mouseup"   ::player/move-end
   "touchend"  ::player/move-end})

(defn pieces [players]
  [:<>
   (for [player-id (keys players)
         :let [positions (get-in players [player-id :position])]]
     ^{:key player-id}
     [:<>
      (for [[index, [x, y]] (map-indexed vector positions)
            :let [userfeedback-handler (fn [e]
                                         (piece/userfeedback-handler* dispatch-map
                                                                      (-> e
                                                                          piece/e-to-event
                                                                          (assoc :player-id player-id)
                                                                          (assoc :index index))))]]
        (piece/piece :x x
                     :y y
                     :size piece/piece-size
                     :id (gstring/format "%s-%s-%s" classname (name player-id), index)
                     :class [classname, "player", (name player-id)]
                     :extra-opts {:on-mouse-down userfeedback-handler
                                  :on-mouse-up userfeedback-handler
                                  :on-mouse-move userfeedback-handler
                                  :on-touch-start userfeedback-handler
                                  :on-touch-end userfeedback-handler
                                  :on-touch-move userfeedback-handler}))])])

(comment (gstring/format "player piece %s"))
