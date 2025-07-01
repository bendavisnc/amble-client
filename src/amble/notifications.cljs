(ns amble.notifications
  (:require
   ["noty" :as Noty]
   [re-frame.core :as re-frame]))

(re-frame/reg-fx
  :notifications
  (fn [message]
    (let [defaults {:layout "topRight"
                    :timeout 1000}]
      (.show (new Noty (clj->js (merge defaults message)))))))
    ;; Here you would implement the actual notification logic, e.g., using a library or custom code
