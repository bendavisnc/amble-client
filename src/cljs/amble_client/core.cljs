(ns amble-client.core
  (:require [cljs.core.async :as async]
            [integrant.core :as ig]
            [reagent.dom :as reagent-dom]
            [reagent.ratom :as reagent-ratom]
            [amble-client.board-pieces]
            [amble-client.board :as amble-board]
            [amble-client.app :as amble-app]
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


(def app-config {:amble/app {:board (ig/ref :amble/board)}
                 :amble/board {:board-pieces (ig/ref :amble/board-pieces)
                               :player-pieces 7}
                 :amble/board-pieces {:state-handler (fn [] (-> app-atom deref :board-pieces))}})

(defn mount-root []
  ;; (println "Invoking reagent/react.")
  (let [app-config-initialized (ig/init app-config)
        _ (.log js/console app-config-initialized)
        _ (println app-config-initialized)]
    (reagent-dom/render [(:amble/app app-config-initialized)]
                        (.getElementById js/document "app"))))

(defn init! []
  (mount-root))

(go
  (let [board-response (async/<! (board-resource/get! "TheSaturdayGame"))
        board-pieces (mapv (fn [c]
                             (let [[x, y] c]
                               {:x x
                                :y y}))
                           board-response)]
    (swap! app-atom assoc :board-pieces board-pieces)))



(defn post-game! []
  (async/take! (game-resource/create!)
               (fn [game-create-response]
                 (println "Requested new game.")
                 (println game-create-response))))

(defn wut []
  (println (deref app-atom)))

