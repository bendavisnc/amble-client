(ns amble-client.core
  (:require
    [reagent.dom :as rdom]
    [amble-client.resource.game :as game-resource]
    [amble-client.markup.core :as markup]
    [amble-client.utils :as utils]
    [amble-client.state :as amble-client-state]
    [amble-client.interaction.core :as amble-client-interaction]))

(defn init-ui-promise! []
  (new js/Promise (fn [resolve, reject]
                    (rdom/render
                      [markup/app-markup-error-checked]
                      (.getElementById js/document "app")
                      (fn [& _]
                        (resolve nil))))))


(defn init!*
  ([]
   (let [game-id (utils/game-id-from-window)
         game-promise (game-resource/get! game-id)
         on-successful-response (fn [game-response]
                                  (if (not (:success game-response))
                                    (throw (new js/Error (:error-text game-response))))
                                  (let [{:keys [designatee-coords, piece-indexes]}
                                        (:body game-response)]
                                    (init!* :game-id game-id
                                            :designatee-coords designatee-coords
                                            :piece-indexes piece-indexes)))]
     (-> game-promise
         (.then on-successful-response)
         (.catch (fn [err]
                   (amble-client-state/update! :errors [err])
                   (throw err))))))

  ([& {:keys [game-id, designatee-coords, piece-indexes]}]
   (let [state-init-promise
         (amble-client-state/init-promise! :game-id game-id
                                           :designatee-coords designatee-coords
                                           :piece-indexes piece-indexes)
         ui-init-promise (init-ui-promise!)]
     (.then
       (.then
         (.then state-init-promise)
         ui-init-promise)
       (fn []
         (amble-client-interaction/init!)
         (println (str "Finished initializing game, "
                       game-id)))))))




(defn init! []
  (.log js/console "Starting client init.")
  (init!*))

(defn post-game! []
  (.then (game-resource/create!)
         (fn [response-result]
           (.log js/console "Requested new game.")
           (.log js/console response-result))))




