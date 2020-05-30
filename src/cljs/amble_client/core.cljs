(ns amble-client.core
  (:require
    [clojure.string :as string]
    [reagent.core :as reagent :refer [atom]]  
    [reagent.dom :as rdom]
    [amble-client.resource.game :as game-resource]))

(defn game-tag-from-location []
  (let [game-id
        (-> js/window
            (aget "location")
            (aget "pathname")
            (string/split "/") 
            last)]
    (when (not game-id)
      (throw (new js/Error "No game id found in browser url.")))
    game-id))

(defn init!*
  "Infer a game id.
   First, see if find by tag returns anything.
   If not, post, and try one more time."
  []
  (.then (game-resource/search! (game-tag-from-location))
         (fn [search-result]
           (.log js/console "hi")
           (.log js/console (clj->js search-result)))))


(defn wut []
  [:div {:class "neat" :id "neato"} (str "This game is named "
                                         (game-tag-from-location))])

(defn init-ui! []
  (rdom/render [wut] (.getElementById js/document "app")))

(defn init-secret-post-game! []
  (let [post-fn
        (fn []
          (.then (game-resource/create!)
                 (fn [response-result]
                   (.log js/console "Requested new game.")
                   (.log js/console response-result))))]
    (aset js/window "amble" (clj->js {:game-create
                                      post-fn}))))


(defn init! []
  (.log js/console "Starting client init.")
  (init!*)
  (init-ui!)
  (init-secret-post-game!))

; (.addEventListener js/document
;                    "keypress"
;                    (fn [e]
;                      (.log js/console "meh.")
;                      (.log js/console (clj->js (martian/explore m)))))

; (.addEventListener js/document)
    
    
