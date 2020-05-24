(ns amble-client.core
  (:require
   [clojure.string :as string]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

; See if the game that we're pointing at already exists.
; If it doesn't, create it first.



(defn begin-websockets! [game-id]
  (.log js/console game-id))

(defn get-or-create-game! [game-id]
  (game-resource/get game-id))


(defn game-id-from-location []
  (-> js/window
      (aget "location")
      (aget "pathname")
      (string/split "/") 
      last))


(defn wut []
  [:div {:class "neat" :id "neato"} (str "This game is named "
                                         (game-id-from-location))])

(defn init![]
  (rdom/render [wut] (.getElementById js/document "app"))
  (begin-websockets!
    (get-or-create-game! game-id-from-location)))
