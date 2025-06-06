(ns amble.server.websockets
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-event-db
  ::cast
  (fn [db [_ {:keys [player, moves]}]]
    (throw (new js/Error [player, moves]))))
