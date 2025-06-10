;; filepath: /home/ben/home/programming/amble/amble-client/src/amble/models/dev.cljs
(ns amble.models.dev
  (:require
   [amble.models.models :refer [db-to-game-id]]
   [amble.server.server :as server]
   [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
  ::keypress
  (fn [cofx [_ e]]
    (let [keypress-key-event (keyword "amble.models.dev"
                                      (str "keypress-" (.-key e)))

          keypress-supported? (#{::keypress-d, ::keypress-p}
                               keypress-key-event)]
      ;; _ (.dir js/console (clj->js [keypress-supported? keypress-key-event ::keypress-d]))
      {:db (:db cofx)
       :fx [(when keypress-supported? [:dispatch [keypress-key-event]])]})))

(re-frame/reg-event-db
  ::keypress-p
  (fn [db, _]
    (println (:game db))))

(re-frame/reg-event-fx
  ::on-game-delete-failure
  (fn [coeff, [_ event]]
    (println "Failed to delete game: " event)))

(re-frame/reg-event-fx
  ::on-game-delete-success
  (fn [_, [_ event]]
    (println "Game deleted successfully: " event)))

(re-frame/reg-event-fx
  ::keypress-d
  (fn [cofx [_ _]]
    (let [game-id (db-to-game-id (:db cofx))]
      {:dispatch [::server/delete-game game-id ::on-game-delete-success, ::on-game-delete-failure]})))

(defn init-keypress-listener! []
  (.addEventListener js/window "keypress"
    (fn [e] (re-frame/dispatch [::keypress e]))))

(defonce _ (init-keypress-listener!))

(comment (keyword (str *ns*) "keypress-d"))
(comment ::keypress-d)
(comment (= ::keypress-d
            (keyword (str *ns*) "keypress-d")))
(comment (#{1} 2))
(comment (str *ns*))
