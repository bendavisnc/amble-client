(ns amble-client.core
  (:require
    [reagent.dom :as rdom]
    [amble-client.resource.game :as game-resource]
    [amble-client.markup.core :as markup]
    [amble-client.utils :as utils]
    [amble-client.state :as amble-client-state]
    [amble-client.interaction.core :as amble-client-interaction]
    [cljs.core.async :as casync]))

(defn init!*
  ([]
   (let [game-id (utils/game-id-from-window)
         game-chan (game-resource/get! game-id)
         on-successful-response (fn [game-response]
                                  (if (not (:success game-response))
                                    (throw (new js/Error (:error-text game-response))))
                                  (let [{:keys [designatee-coords, piece-indexes]}
                                        (:body game-response)]
                                    (init!* :game-id game-id
                                            :designatee-coords designatee-coords
                                            :piece-indexes piece-indexes)))]
     (casync/take! game-chan on-successful-response)))

  ([& {:keys [game-id, designatee-coords, piece-indexes]}]
   (let [on-after-ui-init (fn [_]
                            (amble-client-interaction/init!)
                            (println (str "Finished initializing game, "
                                           game-id)))]
     (amble-client-state/init! :game-id game-id
                               :designatee-coords designatee-coords
                               :piece-indexes piece-indexes)
     (rdom/render
       [markup/app-markup-error-checked]
       (.getElementById js/document "app")
       on-after-ui-init))))

(defn init! []
  (println "Starting client init.")
  (init!*))

(defn post-game! []
  (casync/take! (game-resource/create!)
                (fn [game-create-response]
                  (println "Requested new game.")
                  (println js/console game-create-response))))
