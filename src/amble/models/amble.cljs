(ns amble.models.amble
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 :initialize
 (fn [_ _]
   {}))

(re-frame/reg-sub
  ::amble
  (fn []
    [])
  (fn [[]]
    {}))

