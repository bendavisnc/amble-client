(ns amble.server.server
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
  ::post-game
  (fn [& args]
    (throw (new js/Error "not implemented, game post"))))

(re-frame/reg-event-fx
  ::delete-game
  (fn [& args]
    (throw (new js/Error "not implemented, game delete"))))

(re-frame/reg-event-fx
  ::game-get-default-id
  (fn [& args]
    (throw (new js/Error "not implemented, id get"))))

(re-frame/reg-event-fx
  ::player-get-all-by-game-id
  (fn [& args]
    (throw (new js/Error "not implemented, player get all"))))

(re-frame/reg-event-fx
  ::player-get-by-id
  (fn [& args]
    (throw (new js/Error "not implemented, player get"))))

(re-frame/reg-event-fx
  ::player-move-add
  (fn [& args]
    (throw (new js/Error "not implemented, player move add"))))

(re-frame/reg-event-fx
  ::player-move-get
  (fn [& args]
    (throw (new js/Error "not implemented, player move get"))))
