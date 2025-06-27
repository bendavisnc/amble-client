(ns amble.views.amble
  (:require
   [amble.models.amble :as models]
   [amble.models.pieces.board.pieces :as board-pieces-model]
   [amble.views.error :as error]
   [amble.views.pieces.board :as board-pieces]
   [amble.views.pieces.piece :as piece]
   [amble.views.pieces.player :as player-pieces]
   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :as re-frame]))

(def dispatch-map
  {"mousemove" ::board-pieces-model/move-update
   "touchmove" ::board-pieces-model/move-update})

(defn userfeedback-handler [e]
  (piece/userfeedback-handler* dispatch-map
                               (-> e
                                   piece/e-to-event)))

(defn amble-component [{:keys [board, player-selected, players, landing-piece]}]
  [:div#amble
   [:div#board-container
    [:svg#board {:class (some->> player-selected
                                 name
                                 (gstring/format "%s-sixoclock"))
                 :view-box "0 0 1 1"
                 :ref (fn [target]
                        (when target
                          (println "Setting up board event listeners for: " target)
                          (.addEventListener target "mousemove" userfeedback-handler)
                          (.addEventListener target "touchmove" userfeedback-handler)))}
     [board-pieces/pieces {:pieces-seq (board :pieces)
                           :active-index (board :active-index)}]
     [player-pieces/pieces {:players players
                            :landing-piece landing-piece}]]]])

(defn amble []
  (let [model (re-frame/subscribe [::models/amble])
        errors (re-frame/subscribe [::models/errors])]
    (fn []
      (if-let [error (first @errors)]
        [error/component error]
        [amble-component @model]))))

(comment (first nil))
