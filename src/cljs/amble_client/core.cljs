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

;; (def game-id (utils/game-id-from-window))
;; (assert game-id "Problem getting game-id from browser url.")
(def app-atom (reagent-ratom/atom {}))


(def app-config {:amble/app {:board (ig/ref :amble/board)}
                 :amble/board {:board-pieces (ig/ref :amble/board-pieces)
                               :player-pieces (ig/ref :amble/player-pieces)}
                 :amble/board-pieces {:state-handler (fn [] (-> app-atom deref :board-pieces))}
                 :amble/player-pieces {:state-handler (fn [] (-> app-atom deref :player-pieces))}})

(defn mount-root []
  ;; (println "Invoking reagent/react.")
  (let [app-config-initialized (ig/init app-config)
        _ (.log js/console app-config-initialized)
        _ (println app-config-initialized)]
    (reagent-dom/render [(:amble/app app-config-initialized)]
                        (.getElementById js/document "app"))))

(defn init! []
  (mount-root))

(defn- is-valid-game-id? [id]
  (and id
       (pos? (count id))))

;; Set up board pieces from server game state.
(go
  (let [game-response (let [game-response-first-attempt (async/<! (game-resource/get! (utils/game-id-from-window)))]
                        (if (is-valid-game-id? (:game-id game-response-first-attempt))
                          (do
                            (println (str "Using game id provided from browser address, \"" (:game-id game-response-first-attempt) "\"."))
                            game-response-first-attempt)
                          (let [_ (println (str "Game not found with id, \"" (:game-id game-response-first-attempt) "\"."))
                                game-response-create-attempt (async/<! (game-resource/create!))
                                _ (println game-response-create-attempt)]
                            game-response-create-attempt)))
        game-id (:game-id game-response)
        board-response (async/<! (board-resource/get! game-id))
        board-pieces (mapv (fn [c]
                             (let [[x, y] c]
                               {:x x
                                :y y}))
                           board-response)

        players-response (async/<! (player-resource/get! game-id))
        player-pieces (async/<! (async/into {}
                                            (async/merge
                                             (for [player-id players-response]
                                               (async/pipe (player-resource/get! game-id player-id)
                                                           (async/chan 1
                                                                       (map (fn [coordinates]
                                                                              [(keyword player-id) coordinates]))))))))]
    (swap! app-atom assoc :board-pieces board-pieces)
    (swap! app-atom assoc :player-pieces player-pieces)))



(defn post-game! []
  (async/take! (game-resource/create!)
               (fn [game-create-response]
                 (println "Requested new game.")
                 (println game-create-response))))

(defn wut []
  (println (deref app-atom)))

