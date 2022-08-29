(ns amble-client.settings
  (:require [integrant.core :as ig]))

(defn settings []
  (fn []
    [:div [:span "settings, coming soon"]]))

(defmethod ig/init-key :amble/settings [_ {:keys []}]
  (settings))


