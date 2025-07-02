(ns amble.views.amble
  (:require
   [amble.models.amble :as models]
   [amble.models.pieces.board.pieces :as board-pieces-model]
   [amble.specs.amble :as amble-specs]
   [amble.views.board :as board]
   [amble.views.controls :as controls]
   [amble.views.error :as error]
   [amble.views.pieces.board :as board-pieces]
   [amble.views.pieces.piece :as piece]
   [amble.views.pieces.player :as player-pieces]
   [clojure.spec.alpha :as spec]
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
  (let [board-pieces* [board-pieces/pieces {:pieces-seq (some-> board :pieces)
                                            :active-index (some-> board :active-index)}]
        player-pieces* [player-pieces/pieces {:players players
                                              :landing-piece landing-piece}]]
    [:div#amble
     [controls/component]
     [board/component {:board-pieces board-pieces*
                       :player-pieces player-pieces*
                       :player-selected player-selected
                       :userfeedback-handler userfeedback-handler}]]))

(defn amble []
  (let [model (re-frame/subscribe [::models/amble])]
    (fn []
      [error/boundary [amble-component @model]])))

(comment (first nil))
