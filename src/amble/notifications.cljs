(ns amble.notifications
  (:require
   ["noty" :as Noty]
   [re-frame.core :as re-frame]))

(re-frame/reg-fx
  :notifications
  (fn [message]
    (let [defaults {:layout "topRight"
                    :timeout 1000
                    :animation {:open "animate__animated animate__slideInRight"
                                :close "animate__animated animate__slideOutRight"}}]
      (.show (new Noty (clj->js (merge defaults message)))))))
