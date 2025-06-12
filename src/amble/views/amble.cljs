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
  (let [event (piece/e-to-event e)]
    (doseq [element (.elementsFromPoint js/document (:client-x event) (:client-y event))]
      (when (.contains (.-classList element)
                       "board-piece")
        (let [index-board-piece (some-> element
                                        (.getAttribute "data-index")
                                        js/parseInt)]
          ;; Dispatch the active index to the board model
          (if (int? index-board-piece)
            (do (js/console.info "Setting active index for board piece:" index-board-piece) 
                (re-frame/dispatch [::board/active-index {:index index-board-piece}]))
            (js/console.warn "Invalid index for board piece:" index-board-piece)))))
    (piece/userfeedback-handler* dispatch-map event)))

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
