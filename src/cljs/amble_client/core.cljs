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

(def game-id-atom (atom nil))

(add-watch game-id-atom 
           ::game-id-watch-key 
           (fn [& args]
             (.log js/console "okay boys")
             (.log js/console (deref game-id-atom))))

 


; (defn begin-websockets! [game-id]
;   (.log js/console game-id)
;   (.log js/console "hello there"))

; (defn get-or-create-game! [game-id]
;   (let [existing-game-promise (game-resource/get! game-id)]
;     (.then existing-game-promise
;            (fn [game-response]
;              (.log js/console "alright")
;              (.log js/console (clj->js game-response))))))
             
  ; (game-resource/get game-id))


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

(defn init-game-id! []
  (.then (game-resource/search! (game-tag-from-location))
         (fn [games-found]
           (let [game-id
                 (first (:body games-found))]
             (reset! game-id-atom game-id)))))               
  

(defn wut []
  [:div {:class "neat" :id "neato"} (str "This game is named "
                                         (game-tag-from-location))])

(defn init-ui! [])
  (rdom/render [wut] (.getElementById js/document "app"))

(defn init! []
  (.log js/console "Starting client init.")
  (init-game-id!)
  (init-ui!))

; (.addEventListener js/document
;                    "keypress"
;                    (fn [e]
;                      (.log js/console "meh.")
;                      (.log js/console (clj->js (martian/explore m)))))

; (.addEventListener js/document)
    
    
