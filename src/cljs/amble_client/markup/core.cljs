(ns amble-client.markup.core
  (:require [amble-client.markup.state :as markup-state]))

(defn app-markup-error []
  [:div {:id "badnews" :title (first (markup-state/get :errors))}
   "bad news"])

(defn init-player-pieces![init-coord]
  (player-key init-coord))

(defn board-markup []
  (aset js/window "wut"
        (clj->js
          (markup-state/get :game-placement)))

  [:svg {:id "board" "viewBox" "0 0 1 1"}
   (map
     (fn [[x, y]]
       (println "wut")
       [:circle {:cx x,
                 :cy y
                 :r 0.02
                 :class "stationarygroup"
                 :on-click (fn [] (init-player-pieces!
                                   [x, y]))}])
     (markup-state/get :game-placement))])

(defn app-markup []
  [:div {:id "amble"} (board-markup)])

(defn app-markup-error-checked []
  (if (first (markup-state/get :errors))
    [app-markup-error]
    [app-markup]))
