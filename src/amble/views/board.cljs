(ns amble.views.board
  (:require
   [amble.amble :refer [player-to-index]]
   [goog.string :as gstring]))

(defn component
  [{:keys [board-pieces player-pieces userfeedback-handler player-selected, player-index]}]
  (let [rotation (* 60 player-index)]
    [:div#board-container 
    ;;  {:style {:z-index 1}}
     [:svg#board {:style {:transform (gstring/format "rotate(%ddeg)" rotation)}
                  :class (some->> player-selected name (gstring/format "%s-sixoclock"))
                  :view-box "0 0 1 1"
                  :ref (fn [target]
                         (when target
                           (println "Setting up board event listeners for: " target)
                           (.addEventListener target "mousemove" userfeedback-handler)
                           (.addEventListener target "touchmove" userfeedback-handler)))}
      [:<>
       board-pieces
       player-pieces]]]))
