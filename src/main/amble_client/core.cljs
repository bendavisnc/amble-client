(ns amble-client.core
  (:require [cljs.core.async :as async]
            [integrant.core :as ig]
            ;; [react] 
            [reagent.dom :as reagent-dom]
            [reagent.ratom :as reagent-ratom]
            [amble-client.board-pieces]
            [amble-client.board]
            [amble-client.moves]
            [amble-client.settings]
            [amble-client.main-menu]
            [amble-client.board-piece-closest]
            [amble-client.board-piece-active]
            [amble-client.app]
            [amble-client.player-pieces]
            [amble-client.resource.board :as board-resource]
            [amble-client.resource.game :as game-resource]
            [amble-client.resource.player :as player-resource]
            [amble-client.resource.move :as move-resource]
            [amble-client.utils :as utils]
            [amble-client.game-play]
            [amble-client.move-send]
            [amble-client.move-async]
            [amble-client.move-local]
            [amble-client.move-remote]
            [amble-client.move-receive]
            [amble-client.move-record-check])
  (:require-macros [cljs.core.async :refer [go]]))

;; (def game-id (utils/game-id-from-window))
;; (assert game-id "Problem getting game-id from browser url.")
(def app-atom (reagent-ratom/atom {}))
(def app-ready-chan (async/chan))
(def app-ready-chan-multicast (async/mult app-ready-chan))
(def move-local-chan (async/chan))
(def move-remote-chan (async/chan))
(def move-local-chan-multicast (async/mult move-local-chan))
(def move-remote-chan-multicast (async/mult move-remote-chan))
(def move-xy-chan (async/chan))
(def move-xy-chan-multicast (async/mult move-xy-chan))
(def latest-move-index-chan (async/chan))

(defn move-local-chan-dup []
  (let [c (async/chan)]
    (async/tap move-local-chan-multicast c)
    c))

(defn move-remote-chan-dup []
  (let [c (async/chan)]
    (async/tap move-remote-chan-multicast c)
    c))

(defn move-xy-chan-dup []
  (let [c (async/chan)]
    (async/tap move-xy-chan-multicast c)
    c))

(defn app-ready-chan-dup []
  (let [c (async/chan)]
    (async/tap app-ready-chan-multicast c)
    c))

(def app-config {:amble/app {:app-atom app-atom
                             :main-menu (ig/ref :amble/main-menu)
                             :board (ig/ref :amble/board)
                             :moves (ig/ref :amble/moves)
                             :settings (ig/ref :amble/settings)}

                 :amble/board {:board-pieces (ig/ref :amble/board-pieces)
                               :player-pieces (ig/ref :amble/player-pieces)
                               :game-play (ig/ref :amble/game-play)}

                 :amble/moves {}
                 :amble/settings {}

                 :amble/main-menu {:app-atom app-atom
                                   :app-ready-chan (app-ready-chan-dup)}
                 :amble/board-pieces {:app-atom app-atom}
                 :amble/player-pieces {:app-atom app-atom
                                       :game-play (ig/ref :amble/game-play)
                                       :move-xy-chan (move-xy-chan-dup)}
                 :amble/game-play {:move-local-chan move-local-chan
                                   :move-xy-chan move-xy-chan
                                   :board-piece-closest (ig/ref :amble/board-piece-closest)}
                 :amble/move-send {:move-local-chan (move-local-chan-dup)
                                   :move-resource-add! move-resource/add!}
                 :amble/move-receive {:move-resource-get! move-resource/get!
                                      :latest-move-index-chan latest-move-index-chan
                                      :move-remote-chan move-remote-chan
                                      :app-atom app-atom}
                 :amble/move-async {:latest-move-index-chan latest-move-index-chan
                                    :app-atom app-atom
                                    :app-ready-chan (app-ready-chan-dup)}
                 :amble/move-local {:app-atom app-atom
                                    :move-local-chan (move-local-chan-dup)
                                    :app-ready-chan (app-ready-chan-dup)}
                 :amble/move-remote {:app-atom app-atom
                                     :app-ready-chan (app-ready-chan-dup)
                                     :move-remote-chan (move-remote-chan-dup)
                                     :move-record-check (ig/ref :amble/move-record-check)}
                 :amble/move-record-check {:move-local-chan (move-local-chan-dup)}

                 :amble/board-piece-closest {:app-atom app-atom
                                             :app-ready-chan (app-ready-chan-dup)}
                 :amble/board-piece-active {:app-atom app-atom
                                            :move-xy-chan (move-xy-chan-dup)
                                            :board-piece-closest (ig/ref :amble/board-piece-closest)
                                            :app-ready-chan (app-ready-chan-dup)}})

;;  :amble/app-atom app-atom
                ;;  :amble/user-feedback-handler user-feedback-handler/handle-ui-event

(defn mount-root []
  (let [app-config-initialized (ig/init app-config)
        _ (.log js/console app-config-initialized)]
    (reagent-dom/render [(:amble/app app-config-initialized)]
                        (.getElementById js/document "app")
                        (fn []
                          (async/put! app-ready-chan true)))))

(defn init! []
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
      (swap! app-atom assoc :board-pieces (vec (for [[i, [x,y]] (map-indexed vector board-response)]
                                                 {:x x
                                                  :y y
                                                  :index i
                                                  :is-active? false})))
      (swap! app-atom assoc :player-pieces player-pieces)
      (mount-root))))

(defn post-game! []
  (async/take! (game-resource/create!)
               (fn [game-create-response]
                 (println "Requested new game.")
                 (println game-create-response))))