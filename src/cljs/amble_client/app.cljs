(ns amble-client.app
  (:require [integrant.core :as ig]))

(def app-id "amble")

(defn app [board, main-menu]
  (fn []
    [:div {:style {"position" "fixed", "left" "80px", "top" "89px"}}
      "nuts"]))
    ;; [:div {:id app-id}]))
    
    ;;  [main-menu]
    ;;  [board]]))

(defmethod ig/init-key :amble/app [_ {:keys [board, main-menu]}]
  (app board, main-menu))


