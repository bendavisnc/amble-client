(ns amble.models.amble
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-event-db
  :initialize
  (fn [_ _]
    ;; todo
    {:player-selected :player-one}))

(re-frame/reg-sub
  ::player-selected
  (fn [db, _]
    (:player-selected db)))

(re-frame/reg-sub
  ::amble
  (fn []
    [(re-frame/subscribe [::player-selected])])
  (fn [[player-selected]]
    {:player-selected player-selected}))
