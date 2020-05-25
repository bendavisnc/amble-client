(ns amble-client.core
  (:require
   [clojure.string :as string]
   [reagent.core :as reagent :refer [atom]]  
   [reagent.dom :as rdom]
   [amble-client.resource.game :as game-resource]))
; (let [m (martian-http/bootstrap-swagger "https://pedestal-api.herokuapp.com/swagger.json")]
; (martian/response-for m :create-pet {:name "Doggy McDogFace" :type "Dog" :age 3})
; ;; => {:status 201 :body {:id 123}}

; (martian/response-for m :get-pet {:id 123}))
;; => {:status 200 :body {:name "Doggy McDogFace" :type "Dog" :age 3}}

; See if the game that we're pointing at already exists.
; If it doesn't, create it first.

(defn begin-websockets! [game-id]
  (.log js/console game-id)
  (.log js/console "hello there"))

(defn get-or-create-game! [game-id]
  (let [existing-game-promise (game-resource/get! game-id)]
    (.then existing-game-promise
           (fn [game-response]
             (.log js/console "alright")
             (.log js/console (clj->js game-response))))))
             
  ; (game-resource/get game-id))


(defn game-id-from-location []
  (let [game-id
        (-> js/window
            (aget "location")
            (aget "pathname")
            (string/split "/") 
            last)]
    (when (not game-id)
      (throw (new js/Error "No game id found in browser url.")))
    game-id))


  

(defn wut []
  [:div {:class "neat" :id "neato"} (str "This game is named "
                                         (game-id-from-location))])

(defn init![]
  (.log js/console "Starting client init.")
  (rdom/render [wut] (.getElementById js/document "app"))
  (begin-websockets!
    (get-or-create-game! (game-id-from-location))))

; (.addEventListener js/document
;                    "keypress"
;                    (fn [e]
;                      (.log js/console "meh.")
;                      (.log js/console (clj->js (martian/explore m)))))

; (.addEventListener js/document)
(.dir js/console js/document)
    
    
