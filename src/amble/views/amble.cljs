(ns amble.views.amble
  (:require
   [amble.models.amble :as models]
   [amble.models.pieces.board.board :as board]
   [amble.views.pieces.board :as board-pieces]
   [amble.views.pieces.piece :as piece]
   [amble.views.pieces.player :as player-pieces]
   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :as re-frame]))

(def dispatch-map
  {"mousemove" ::board/move-update
   "touchmove" ::board/move-update})

(defn userfeedback-handler [e]
  (piece/userfeedback-handler* dispatch-map
                               (-> e
                                   piece/e-to-event)))

(defn amble-component [{:keys [board, player-selected, players, landing-piece]}]
  [:div#amble
   [:div#board-container
    [:svg#board {:class (gstring/format "%s-six-oclock" (name player-selected))
                 :view-box "0 0 1 1"
                 :on-mouse-move userfeedback-handler}
     [board-pieces/pieces {:pieces-seq (board :pieces)
                           :active-index (board :active-index)}]
     [player-pieces/pieces {:players players
                            :landing-piece landing-piece}]]]])

(defn amble []
  (let [model (re-frame/subscribe [::models/amble])]
    (fn []
      ;; (str @model))))
      [amble-component @model])))

(comment (name :hello))
