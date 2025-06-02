(ns amble.views.amble
  (:require
   [amble.models.amble :as models]
   [amble.specs.amble :as spec]
   [amble.static-content.board :as static-board]
   [amble.views.pieces.board :as board-pieces]
   [amble.views.pieces.player :as player-pieces]
   [cljs.math :as math]
   [clojure.spec.alpha :as s]
   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :as re-frame]))

(defn amble-component [{:keys [player-selected, players]}]
  (let [board-pieces (vec (for [[i, [x,y]] #_{:clj-kondo/ignore [:unresolved-var]}
                                (map-indexed vector static-board/board)]
                            {:x x
                             :y y
                             :index i
                             :is-active? false}))]
    [:div#amble
     [:div#board-container
      [:svg#board {:class (gstring/format "%s-six-oclock" (name player-selected))
                   :view-box "0 0 1 1"}
       [board-pieces/pieces board-pieces]
       [player-pieces/pieces players]]]]))

(defn amble []
  (let [model (re-frame/subscribe [::models/amble])]
    (fn []
      ;; (str @model))))
      [amble-component @model])))

(comment (name :hello))
