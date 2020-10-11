(ns amble-client.core
  (:require
    [reagent.dom :as rdom]
    [amble-client.resource.game :as game-resource]
    [amble-client.resource.board :as board-resource]
    [amble-client.markup.core :as markup]
    [amble-client.utils :as utils]
    [amble-client.state :as amble-client-state]
    [amble-client.interaction.core :as amble-client-interaction]
    [cljs.core.async :as casync])
  (:require-macros
    [cljs.core.async :refer [go go-loop]]))


(defn init!* []
  (go
    (let [game-id (utils/game-id-from-window)
          board  (:body (casync/<! (board-resource/get! game-id)))]
      (amble-client-state/init! :game-id game-id
                                :designatee-coords board
                                :piece-indexes [])
      (println "nice")
      (println board))))

  ;([]
  ; (let [game-id (utils/game-id-from-window)
  ;       game-chan (game-resource/get! game-id)
  ;       on-successful-response (fn [game-response]
  ;                                (if (not (:success game-response))
  ;                                  (throw (new js/Error (:error-text game-response))))
  ;                                (let [{:keys [designatee-coords, piece-indexes]}
  ;                                      (:body game-response)]
  ;                                  (init!* :game-id game-id
  ;                                          :designatee-coords designatee-coords
  ;                                          :piece-indexes piece-indexes)
  ;                                  true))]
  ;   (casync/pipeline
  ;     1
  ;     (casync/chan)
  ;     (map on-successful-response)
  ;     game-chan
  ;     true
  ;     (fn [err]
  ;       (amble-client-state/update! :errors [err])
  ;       (throw err)))))
  ;
  ;([& {:keys [game-id, designatee-coords, piece-indexes]}]
  ; (do
  ;   (amble-client-state/init! :game-id game-id
  ;                             :designatee-coords designatee-coords
  ;                             :piece-indexes piece-indexes)
  ;
  ;   (js/setTimeout amble-client-interaction/init! 200)
  ;   (println (str "Finished initializing game, "
  ;                 game-id)))))

(defn init! []
  (println "Starting client init.")
  (rdom/render
    [markup/app-markup-error-checked]
    (.getElementById js/document "app")
    init!*))

(defn post-game! []
  (casync/take! (game-resource/create!)
                (fn [game-create-response]
                  (println "Requested new game.")
                  (println js/console game-create-response))))
