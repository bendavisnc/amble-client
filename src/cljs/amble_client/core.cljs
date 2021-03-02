(ns amble-client.core
  (:require [cljs.core.async :as async]
            [integrant.core :as ig]
            [reagent.dom :as reagent-dom]
            [amble-client.board-pieces]
            [amble-client.board]
            [amble-client.player-pieces]
            [amble-client.resource.board :as board-resource]
            [amble-client.resource.game :as game-resource]
            [amble-client.resource.player :as player-resource]
            [amble-client.async-resource.base]
            [amble-client.async-resource.move]
            [amble-client.resource.move :as move-resource]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go]]))

(defn ig-amble-config [game-id, on-after-render-chan-multap]
  {:amble/board   {:board-pieces (ig/ref :amble/board-pieces)
                   :player-pieces (ig/ref :amble/player-pieces)}
   :amble/board-pieces {:game-id          game-id
                        :resource-chan-fn board-resource/get!}
   :amble/async-resource-base {:game-id game-id}
   :amble/async-resource-move {:async-resource-base (ig/ref :amble/async-resource-base)}

   :amble/player-pieces {:game-id          game-id
                         :resource-chan-fns {:get player-resource/get!
                                             :add move-resource/add!}
                         :remote-control nil
                         :post-init-chan (async/tap on-after-render-chan-multap (async/chan 1))}})

(defn app [board-fn]
  [:div {:id "amble"} [board-fn]])

(defn init! []
  (println "Starting client init!")
  (go (let [game-id (utils/game-id-from-window)
            on-after-render-chan (async/chan 1)
            multtap (async/mult on-after-render-chan)
            ig-amble (ig/init (ig-amble-config game-id
                                               multtap))
            board-fn (async/<! (:amble/board ig-amble))]
        (println "Invoking reagent.")
        (reagent-dom/render (app board-fn)
                            (.getElementById js/document "app")
                            (fn []
                              (async/put! on-after-render-chan true)))
        nil)))

(defn post-game! []
  (async/take! (game-resource/create!)
               (fn [game-create-response]
                 (println "Requested new game.")
                 (println game-create-response))))

