(ns amble-client.core
  (:require [cljs.core.async :as async]
            [integrant.core :as ig]
            [reagent.dom :as reagent-dom]
            [reagent.ratom :as reagent-ratom]
            [amble-client.board-pieces]
            [amble-client.board]
            [amble-client.player-pieces]
            [amble-client.resource.board :as board-resource]
            [amble-client.resource.game :as game-resource]
            [amble-client.resource.player :as player-resource]
            [amble-client.async-resource.base]
            [amble-client.async-resource.move :as async-move-resource]
            [amble-client.resource.move :as move-resource]
            [amble-client.utils :as utils]
            [amble-client.config :as amble-client-config])
  (:require-macros [cljs.core.async :refer [go]]))

(def app-atom (reagent-ratom/atom {}))

(.addEventListener (.-body js/document)
                   "mousemove"
                   (fn [e]
                     (println "neato")
                     (.log js/console e)
                     (swap! app-atom assoc :x (.-clientX e))
                     (swap! app-atom assoc :y (.-clientY e))
                     (.log js/console (deref app-atom))
                     (println @app-atom)))

(defn app []
  (let [s (deref app-atom)]
    [:div {:width "400px"
           :height "300px"}
      [:ol
        [:li (str "x: " (s :x))]
        [:li (str "y: " (s :y))]
        [:li (str "coord: " s)]]
      [:button "neat button"]]))

(defn mount-root []
  (println "Invoking reagent/react.")
  (reagent-dom/render [app] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
