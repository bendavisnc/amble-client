(ns amble-client.core
  (:require
   [clojure.string :as string]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [martian.core :as martian]
   [martian.cljs-http :as martian-http]))

; (let [m (martian-http/bootstrap-swagger "https://pedestal-api.herokuapp.com/swagger.json")]
; (martian/response-for m :create-pet {:name "Doggy McDogFace" :type "Dog" :age 3})
; ;; => {:status 201 :body {:id 123}}

; (martian/response-for m :get-pet {:id 123}))
;; => {:status 200 :body {:name "Doggy McDogFace" :type "Dog" :age 3}}

; See if the game that we're pointing at already exists.
; If it doesn't, create it first.


(defn begin-websockets! [game-id]
  (.log js/console game-id
  (.log js/console "hello there")))

(defn get-or-create-game! [game-id]
  nil)
  ; (game-resource/get game-id))


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
  (.log js/console "hellllllllllo")
  (let [m (martian-http/bootstrap-swagger "http://localhost:3449/openapi.json")]
    (rdom/render [wut] (.getElementById js/document "app"))
    (begin-websockets!
      (get-or-create-game! game-id-from-location))))

(.addEventListener js/document
                   "keypress"
                   (fn [e]
                     (.log js/console "meh.")
                     (.log js/console (clj->js (martian/explore m)))))

; (.addEventListener js/document)
(.dir js/console js/document)
    
    
