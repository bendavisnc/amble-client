(ns amble.views.board
  (:require
   [goog.string :as gstring]
   [reagent.core :as reagent]))

(defn component
  [{:keys [board-pieces player-pieces userfeedback-handler player-selected]}]
  [:div#board-container
   [:svg#board {:class (some->> player-selected name (gstring/format "%s-sixoclock"))
                :view-box "0 0 1 1"
                :ref (fn [target]
                       (when target
                         (println "Setting up board event listeners for: " target)
                         (.addEventListener target "mousemove" userfeedback-handler)
                         (.addEventListener target "touchmove" userfeedback-handler)))}
    [:<>
     board-pieces
     player-pieces]]])
