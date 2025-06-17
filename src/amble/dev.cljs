;; filepath: /home/ben/home/programming/amble/amble-client/src/amble/models/dev.cljs
(ns amble.dev
  (:require
   [re-frame.core :as re-frame]))

(defn printdb
  "Prints the current db to the console."
  []
  (re-frame/dispatch [::printdb]))

(re-frame/reg-event-db
 ::printdb
 (fn [db, _]
   (println (:game db))))

(defn index-to-player [i]
  (case i
    0 :player-one
    1 :player-two
    2 :player-three
    3 :player-four
    4 :player-five
    5 :player-six))

(defn setplayer [i]
  (re-frame/dispatch [::setplayer i]))

(re-frame/reg-event-db
 ::setplayer
 (fn [db, [_ i]]
   (assoc-in db 
             [:game :settings :player]
             (index-to-player i))))
   