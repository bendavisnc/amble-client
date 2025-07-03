(ns amble.models.controls
  (:require
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(defn set-next-player [db]
  (println "Setting next player.")
  (update-in db
             [:game :settings :player-index]
             inc))

(defn dec-move-index [db]
  (println "Decrementing move index.")
  (update-in db
             [:game :move-index]
             dec))

(re-frame/reg-event-fx
 ::on-control
 (fn [{:keys [db]}, [_ control-id]]
   (cond (= :rotate control-id)
         {:db (set-next-player db)}
         (= :undo control-id)
         {:db (dec-move-index db)}
         :else
         (throw (new js/Error (gstring/format "Unknown control id, `%s`." control-id))))))
