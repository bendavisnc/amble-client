(ns amble-client.core
  (:require
    [clojure.string :as string]
    [reagent.core :as reagent]
    [reagent.dom :as rdom]
    [amble-client.resource.game :as game-resource]
    [amble-client.utils :as utils]))

(def atomic-state (reagent/atom {:errors []}))

(defn update-app-state! [& args]
  (cond (= 2 (count args))
        (let [[k, v] args]
          (swap! atomic-state update-in [k] conj v))
        :else
        (throw (new js/Error (str "Don't know how to update app state with args, \""
                                  args
                                  "\".")))))

(defn print-app-state! []
  (.log js/console (clj->js (deref atomic-state))))

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
                 (update-app-state! :game-placement game-placement)))
        (.catch (fn [err]
                  (update-app-state! :errors err)
                  (throw err))))
    (println "alright?")))


(defn app-markup-error []
  [:div {:title (-> atomic-state deref :errors first)}
        "bad news"])

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
    
    
