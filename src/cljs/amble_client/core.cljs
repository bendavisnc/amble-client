(ns amble-client.core
  (:require
    [clojure.string :as string]
    [reagent.core :as reagent]
    [reagent.dom :as rdom]
    [amble-client.resource.game :as game-resource]
    [amble-client.markup.core :as markup]
    [amble-client.utils :as utils]
    [amble-client.markup.state :as markup-state]))

(defn pieces [& {:keys [designatee-coords, piece-indexes]}]
  (map (fn [i]
         (or (get designatee-coords i)
             (throw (new js/Error (str "Invalid piece index, " i ".")))))
       piece-indexes))

(defn init!*
  ([]
   (let [
         game-id (utils/game-id-from-window)
         game-promise (game-resource/get! game-id)
         on-successful-response (fn [game-response]
                                  (if (not (:success game-response))
                                    (throw (new js/Error (str "Game not found (" game-id ")."))))
                                  (init!*
                                         (:body game-response)))]

     (-> game-promise
         (.then on-successful-response)
         (.catch (fn [err]
                   (markup-state/update! :errors [err])
                   (throw err))))
     (println (str "Finished initializing game, "
                   game-id
                   "."))))
  ([{:keys [designatee-coords, piece-indexes]}]
   (markup-state/update! :placement :designatee designatee-coords)
   (doseq [i (range (count piece-indexes))]
     (let [player-index (inc i)
           indexes (get piece-indexes i)]
       (markup-state/update! :placement
                             :player
                             player-index
                             (pieces :designatee-coords designatee-coords
                                     :piece-indexes indexes))))
   nil))

(defn init-ui! []
  (let [ui-atom (reagent/atom {})]
    (rdom/render [markup/app-markup-error-checked] (.getElementById js/document "app"))
    ui-atom))

(defn init! []
  (.log js/console "Starting client init.")
  (init!*)
  (init-ui!))

(defn post-game! []
  (.then (game-resource/create!)
         (fn [response-result]
           (.log js/console "Requested new game.")
           (.log js/console response-result))))


; (.addEventListener js/document
;                    "keypress"
;                    (fn [e]
;                      (.log js/console "meh.")
;                      (.log js/console (clj->js (martian/explore m)))))

; (.addEventListener js/document)
    
    
