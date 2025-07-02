(ns amble.models.controls
  (:require
   [amble.amble :refer [index-to-player player-to-index]]
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(defn- set-next-player [db]
  (let [player-id (get-in db [:game :settings :player])
        player-index (player-to-index player-id)
        players-count (count (get-in db [:game :player]))
        next-player-index (mod (inc player-index) players-count)
        player-id-next (index-to-player next-player-index)]
    (assoc-in db
              [:game :settings :player]
              player-id-next)))

(re-frame/reg-event-fx
  ::on-control
  (fn [{:keys [db]}, [_ control-id]]
    (cond (= :rotate control-id)
          {:db (set-next-player db)}
          :else
          (throw (new js/Error (gstring/format "Unknown control id, `%s`." control-id))))))
