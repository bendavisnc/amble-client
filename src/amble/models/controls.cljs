(ns amble.models.controls
  (:require
   [amble.models.amble :as amble]
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(defn set-next-player [db]
  (println "Setting next player.")
  (update-in db
             [:game :settings :player-index]
             inc))

(re-frame/reg-event-db
  ::on-control-rotate
  (fn [db, _]
    (set-next-player db)))

(re-frame/reg-event-fx
  ::on-control-undo
  (fn [{:keys [db]}, [_ _]]
    {:db db
     :dispatch [::amble/move-index-dec]}))

(re-frame/reg-event-fx
  ::on-control-redo
  (fn [{:keys [db]}, [_ _]]
    {:db db
     :dispatch [::amble/move-index-inc]}))
