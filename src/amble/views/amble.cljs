(ns amble.views.amble
  (:require
   [amble.models.amble :as models]
   [amble.models.pieces.board.board :as board]
   [amble.specs.amble :as spec]
   [amble.static-content.board :as static-board]
   [amble.views.pieces.board :as board-pieces]
   [amble.views.pieces.player :as player-pieces]
   [amble.views.pieces.piece :as piece-view]
   [cljs.math :as math]
   [clojure.spec.alpha :as s]
   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :as re-frame]))

(def dispatch-map
  {"mousemove" ::board/move-update
   "touchmove" ::board/move-update})

(def event-to-coord-fn (piece-view/coord-conv))

(defn event-to-coord* [board-elem, e]
  (event-to-coord-fn board-elem e))

(defn e-to-event [e]
  (let [event-to-coord (partial event-to-coord* (.getElementById js/document "board"))
        [x, y] (event-to-coord e)]
    {:x x
     :y y
     :event-type (.-type e)}))

(defn userfeedback-handler* [e]
  (let [event (-> e
                  e-to-event)
        event-type (.-type e)
        action (get dispatch-map event-type)]
    ;; (println [action event])
    (if action
      (re-frame/dispatch [action event])
      (throw (js/Error. (str "No user feedback handler defined for " event-type))))))

(def userfeedback-handler
  userfeedback-handler*)

(defn amble-component [{:keys [board, player-selected, players]}]
  [:div#amble
   [:div#board-container
    [:svg#board {:class (gstring/format "%s-six-oclock" (name player-selected))
                 :view-box "0 0 1 1"
                 :on-mouse-move userfeedback-handler}
     [board-pieces/pieces {:pieces-seq (board :pieces)
                           :active-index (board :active-index)}]
     [player-pieces/pieces players]]]])

(defn amble []
  (let [model (re-frame/subscribe [::models/amble])]
    (fn []
      ;; (str @model))))
      [amble-component @model])))

(comment (name :hello))
