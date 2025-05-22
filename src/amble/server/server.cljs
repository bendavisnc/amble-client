(ns amble.server.server
  (:require [re-frame.core :as re-frame]))

;; Success event handler
(re-frame/reg-event-fx
  ::post-game-success
  (fn [{:keys [db]} [_ response]]
    (println "Game posted successfully!" response)
    {:db db}))
   

;; Failure event handler
(re-frame/reg-event-fx
  ::post-game-failure
  (fn [{:keys [db]} [_ error]]
    (println "Game posted unsuccessfully!" error)
    {:db db}))
     ;; Additional error handling can be done here.
     