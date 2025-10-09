(ns amble.notifications
  (:require
   ["noty" :as Noty]
   [re-frame.core :as re-frame]))

(def noclose-notifications (atom []))

(defn clear-messages! []
  (doseq [n @noclose-notifications]
    (.close n))
  (reset! noclose-notifications []))

(re-frame/reg-fx
  :notifications
  (fn [message]
    (let [defaults {:layout "topRight"
                    :timeout 1000
                    :animation {:open "animate__animated animate__slideInRight"
                                :close "animate__animated animate__slideOutRight"}}
          is-no-close? (= false (:timeout message))
          noty (new Noty (clj->js (merge defaults message)))]
      (clear-messages!)
      (.show noty) 
      (when is-no-close?
        (swap! noclose-notifications conj noty)))))  
