(ns amble-client.core
  (:require
    [clojure.string :as string]
    [reagent.core :as reagent]
    [reagent.dom :as rdom]
    [amble-client.resource.game :as game-resource]
    [amble-client.utils :as utils]))

(def atomic-state (reagent/atom {}))

(defn init!*
  []
  (.then (game-resource/get! (utils/game-id-from-window))
         (fn [search-result]
           (.log js/console "hi hi hello sir")
           (.log js/console (clj->js search-result)))))

(defn app-markup-error []
  "bad news")

(defn app-container []
  (if (-> atomic-state deref :errors first)
    (app-markup-error)
    [:div {:class "neat" :id "neato"} (str "This game is named "
                                           (utils/game-id-from-window))]))

(defn init-ui! []
  (let [ui-atom (reagent/atom {})]
    (rdom/render [app-container] (.getElementById js/document "app"))
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
    
    
