(ns amble-client.core
  (:require
    [clojure.string :as string]
    [reagent.core :as reagent]
    [reagent.dom :as rdom]
    [amble-client.resource.game :as game-resource]
    [amble-client.markup.core :as markup]
    [amble-client.utils :as utils]
    [amble-client.markup.state :as markup-state]))


(defn init!*
  []
  (let [
        game-id (utils/game-id-from-window)
        game-promise (game-resource/get! game-id)
        on-successful-response (fn [game-response]
                                 (if (not (:success game-response))
                                   (throw (new js/Error (str "Game not found (" game-id ")."))))
                                 (js->clj (.parse js/JSON (:body game-response))))]

    (-> game-promise
        (.then on-successful-response)
        (.then (fn [game-placement]
                 (markup-state/update! :game-placement game-placement)))
        (.catch (fn [err]
                  (markup-state/update! :errors [err])
                  (throw err))))
    (println "alright?")))



(defn init-ui! []
  (let [ui-atom (reagent/atom {})]
    (rdom/render [markup/app-markup-error-checked] (.getElementById js/document "app"))
    ui-atom))

(defn init! []
  (.log js/console "Starting client init.")
  (init!*)
  (init-ui!)
  (utils/init-secret-post-game!))


; (.addEventListener js/document
;                    "keypress"
;                    (fn [e]
;                      (.log js/console "meh.")
;                      (.log js/console (clj->js (martian/explore m)))))

; (.addEventListener js/document)
    
    
