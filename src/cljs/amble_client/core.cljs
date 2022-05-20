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
            [amble-client.game-play :as game-play])
  (:require-macros [cljs.core.async :refer [go]]))

;; (def game-id (utils/game-id-from-window))
;; (assert game-id "Problem getting game-id from browser url.")
(def app-atom (reagent-ratom/atom {}))

(defmulti on-move! (fn [& args]
                     (if (-> args last number?)
                       ::move-instance
                       ::move)))

(defmethod on-move! ::move-instance [player-id, player-piece-index, x, y]
  (swap! app-atom assoc-in [:player-pieces player-id player-piece-index] [x, y]))

(defmethod on-move! ::move [player-id, player-piece-index, move]
  (move-resource/add! (:game-id, 
                       (deref app-atom))  
                      (name player-id) 
                      player-piece-index, 
                      move))



(def app-config {:amble/app {:board (ig/ref :amble/board)}
                 :amble/board {:board-pieces (ig/ref :amble/board-pieces)
                               :player-pieces (ig/ref :amble/player-pieces)
                               :game-play (ig/ref :amble/game-play)}
                 :amble/board-pieces {:state-handler (fn [] (-> app-atom deref :board-pieces))}
                 :amble/player-pieces {:state-handler (fn [] (-> app-atom deref :player-pieces))
                                       :game-play (ig/ref :amble/game-play)}
                 :amble/game-play {:on-move! on-move!}})
                ;;  :amble/app-atom app-atom
                ;;  :amble/user-feedback-handler user-feedback-handler/handle-ui-event

(defn mount-root []
  ;; (println "Invoking reagent/react.")
  (let [app-config-initialized (ig/init app-config)
        _ (.log js/console app-config-initialized)
        _ (println app-config-initialized)]
    (reagent-dom/render [(:amble/app app-config-initialized)]
                        (.getElementById js/document "app"))))

(defn init! []
  (mount-root))

;; Set up board pieces from server game state.
(go
  (let [game-response (let [game-id-from-window (utils/game-id-from-window)
                            game-response-first-attempt (async/<! (game-resource/get! game-id-from-window))] 
                        (if (:game-id game-response-first-attempt)
                          (do
                            (println (str "Using game id provided from browser address, \"" game-id-from-window))
                            game-response-first-attempt)
                          (let [_ (println (str "Game not found with id, \"" game-id-from-window))
                                game-response-create-attempt (async/<! (game-resource/create!))
                                _ (println game-response-create-attempt)]
                            game-response-create-attempt)))
        game-id (:game-id game-response)
        board-response (async/<! (board-resource/get! game-id))

        players-response (async/<! (player-resource/get! game-id))
        player-pieces (async/<! (async/into {}
                                            (async/merge
                                             (for [player-id players-response]
                                               (async/pipe (player-resource/get! game-id player-id)
                                                           (async/chan 1
                                                                       (map (fn [coordinates]
                                                                              [(keyword player-id) coordinates]))))))))]
    (swap! app-atom assoc :game-id game-id)
    (swap! app-atom assoc :board-pieces board-response)
    (swap! app-atom assoc :player-pieces player-pieces)))



(defn post-game! []
  (async/take! (game-resource/create!)
               (fn [game-create-response]
                 (println "Requested new game.")
                 (println game-create-response))))

(defn wut []
  (println (deref app-atom)))

