(ns amble.errors
  (:require
   [re-frame.core :as re-frame]))

(aset js/window
      "onerror"
      (fn [message, source, lineno, colno, error]
        (re-frame/dispatch [::error message error])))

(re-frame/reg-event-db
  ::error
  (fn [db [_ message error]]
    (let [error-entry {:message message
                       :error error}]
      (update db ::errors conj error-entry))))
