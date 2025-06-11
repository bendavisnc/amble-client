(ns amble.server.async.server
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
  ::on-message
  (fn [& args]
    (throw (new js/Error "not implemented, on message"))))

(re-frame/reg-event-fx
  ::on-open
  (fn [& args]
    (throw (new js/Error "not implemented, on open"))))

(re-frame/reg-event-fx
  ::on-close
  (fn [& args]
    (throw (new js/Error "not implemented, on close"))))

(re-frame/reg-event-fx
  ::on-error
  (fn [& args]
    (throw (new js/Error ["not implemented, on error", args]))))

(re-frame/reg-event-fx
  ::initialize
  (fn [& args]
    (throw (new js/Error "not implemented, on initialize"))))
