(ns amble.views.amble
  (:require
   [amble.models.amble :as models]
   [amble.specs.amble :as spec]
   [amble.views.pieces.board :as board-pieces]
   [cljs.math :as math]
   [clojure.spec.alpha :as s]
   [goog.string :as gstring]
   [goog.string.format]
   [re-frame.core :as re-frame]))

(def board-response [[0.5, 0.5]
                     [0.66, 0.5]])

(defn amble-component [model]
  (let [player-selected (:player-selected model)
        board-pieces (vec (for [[i, [x,y]] (map-indexed vector board-response)]
                            {:x x
                             :y y
                             :index i
                             :is-active? false}))]
    [:div#amble
     [:div#board-container
      [:svg#board {:class (gstring/format "%s-six-oclock" (name player-selected))}
       [board-pieces/pieces board-pieces]]]]))

(defn amble []
  (let [model (re-frame/subscribe [::models/amble])]
    (fn []
      [amble-component @model])))

(comment (name :hello))
