(ns amble.models.controls
  (:require
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(defn- set-next-player [db]
  (update-in db
             [:game :settings :player-index]
             inc))

(re-frame/reg-event-fx
  ::on-control
  (fn [{:keys [db]}, [_ control-id]]
    (cond (= :rotate control-id)
          {:db (set-next-player db)}
          :else
          (throw (new js/Error (gstring/format "Unknown control id, `%s`." control-id))))))
