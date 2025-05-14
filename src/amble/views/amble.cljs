(ns amble.views.amble
  (:require
   [cljs.math :as math]
   [clojure.spec.alpha :as s]
   [amble.models.amble :as models]
   [amble.specs.amble :as spec]
   [re-frame.core :as re-frame]))

(defn amble-component [model]
  [:div.game
   [:div.game-board [:div "todo"]]])

(defn amble []
  (let [model (re-frame/subscribe [::models/amble])]
    (fn []
      [amble-component @model])))
