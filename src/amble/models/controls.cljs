(ns amble.models.controls
  (:require
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(defn set-next-player [db]
  (println "Setting next player.")
  (update-in db
             [:game :settings :player-index]
             inc))

(re-frame/reg-event-fx
  ::on-control
  (fn [{:keys [db]}, [_ control-id]]
    (cond (= :rotate control-id)
          {:db (set-next-player db)}
          (= :undo control-id)
          {:db db
           :dispatch [:amble.models.amble/move-index-dec]}
          :else
          (throw (new js/Error (gstring/format "Unknown control id, `%s`." control-id))))))
