(ns amble-client.v3.core
  (:require [cljs.core.async :as async]
            [amble-client.resource.game :as game-resource]
            [amble-client.resource.board :as board-resource]
            [amble-client.v3.board :as board]
            [amble-client.utils :as utils]
            [amble-client.v3.global-state :as gs]
            [goog.string :as gstring]
            [goog.string.format]
            [reagent.dom]
            [reagent.core :as reagent])
  (:require-macros [cljs.core.async :refer [go]]))


(defn game-state-at-start [game-id]
  {:current game-id
   game-id {:pieces {:landing [{:position {:x 0.5, :y 0.5}
                                :size 0.023}]}}})

(defn game-fn []
  (let [game-id (utils/game-id-from-window)
        ;; This is the game state that drives the whole client ui with react.
        reagent-atom-gs (reagent/atom (game-state-at-start game-id))]
    (go (let [board-coords (async/<! (board-resource/get! game-id))]
          (println "Setting up board...")
          (println (gstring/format "... using coords, %s." board-coords))
          (gs/set! reagent-atom-gs [game-id :pieces :landing] (for [[x, y] board-coords]      
                                                                {:position {:x x, :y y}
                                                                 :size {:radius 0.023}}))
          (println "wut wut")
          (println (deref reagent-atom-gs))))

    (fn []
      (let [game-state (deref reagent-atom-gs)]
        [:div {:id "amble"}
         [board/board game-state game-id]]))))

(defn mount-root []
  (reagent.dom/render [(game-fn)] 
                      (.getElementById js/document "app")))
                       

(defn init! []
  (mount-root))

(defn post-game! []
  (async/take! (game-resource/create!)
               (fn [game-create-response]
                 (println "Requested new game.")
                 (println game-create-response))))

