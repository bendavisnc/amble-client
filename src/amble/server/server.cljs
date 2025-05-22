(ns amble.server.server
  (:require [re-frame.core :as re-frame]))

;; Success event handler
(re-frame/reg-event-fx
  ::post-game
  (fn [& args]
    (throw (new js/Error "not implemented"))))
   