(ns amble-client.app
  (:require [integrant.core :as ig]))

(def app-id "amble")

(defn app [board, main-menu]
  (fn []
    [:div {:id app-id}
     [main-menu]
     [board]]))

(defmethod ig/init-key :amble/app [_ {:keys [board, main-menu]}]
  (app board, main-menu))


