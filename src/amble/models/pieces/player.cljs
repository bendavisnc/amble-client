(ns amble.models.pieces.player
  (:require
   [amble.server.websockets :as websockets]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 ::move-start
 (fn [db [_ {:keys [player, x, y]}]]
   (-> db
       (assoc-in [:game :player player :move-in-progress :moves]
                 [[x, y]]))))


(re-frame/reg-event-fx
  ::move-end
  (fn [{:keys [db]} [_ {:keys [player]}]]
    (let [moves (-> db (get-in [:game :player player :move-in-progress]))
          move-event {:player player
                      :moves moves}]
      {:fx [[:dispatch [::websockets/cast move-event]]]
       :db (update-in db 
                      [:game :player player] 
                      dissoc 
                      :move-in-progress)})))
 


