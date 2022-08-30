(ns amble-client.settings
  (:require [integrant.core :as ig]
            [reagent.core]))
            ;; ["react-settings-pane" :refer [SettingsPane]]
            ;; [cljsjs.react]))

(defn settings []
  (fn []
    "dern"))
    ;; [:> SettingsPane])) 

(defmethod ig/init-key :amble/settings [_ {:keys []}]
  (settings))


