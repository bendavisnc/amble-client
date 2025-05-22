(ns amble.server.server
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::post-game
 (fn [& args]
   (throw (new js/Error "not implemented"))))

(re-frame/reg-event-fx
  ::game-get-default-id
  (fn [& args]
    (throw (new js/Error "not implemented"))))
   
   