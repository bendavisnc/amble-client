(ns amble-client.v3.core
  (:require [cljs.core.async :as async]
            [amble-client.resource.game :as game-resource]
            [amble-client.v3.board :as board]
            [reagent.dom]
            [reagent.core :as reagent])
  (:require-macros [cljs.core.async :refer [go]]))

(def game-id :neat-game-id)

(def start-game-state {game-id {:pieces {:landing [{:position {:x 0.3 :y 0.3}
                                                    :size {:radius 1}}
                                                   {:position {:x 0.2 :y 0.3}
                                                    :size {:radius 1}}
                                                   {:position {:x 0.2 :y 0.4}
                                                    :size {:radius 1}}]}}
                       :current game-id})



(defn game-fn []
  (let [reagent-atom-gs (reagent/atom start-game-state)]
    (fn []
      (let [game-state (deref reagent-atom-gs)]
        [:div {:id "amble"}
         [board/board game-state game-id]]))))

(defn mount-root []
  (reagent.dom/render [(game-fn)] (.getElementById js/document "app")))

(defn init! []
  (mount-root))

(defn post-game! []
  (async/take! (game-resource/create!)
               (fn [game-create-response]
                 (println "Requested new game.")
                 (println game-create-response))))

