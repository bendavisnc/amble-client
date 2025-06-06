;; filepath: /home/ben/home/programming/amble/amble-client/src/amble/models/dev.cljs
(ns amble.models.dev
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-event-fx
 ::keypress
 (fn [cofx [_ e]]
   (let [keypress-key-event (keyword (str *ns*) 
                                     (str "keypress-" (.-key e)))

         keypress-supported? (= (name keypress-key-event) 
                                (name ::keypress-d))]
        ;;  _ (.dir js/console (clj->js [keypress-supported? keypress-key-event]))]
     {:db (:db cofx)
      :fx [(when keypress-supported? [:dispatch [::keypress-d]])]})))

(re-frame/reg-event-db
 ::keypress-d
 (fn [db, _]
   (println (:game db))
   (println "Key pressed, dispatching ::keypress")))

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