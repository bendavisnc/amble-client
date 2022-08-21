(ns amble-client.app
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(def app-id "amble")

(defn app [board]
  (fn []
    [:div {:id app-id} 
      [board]]))

(defmethod ig/init-key :amble/app [_ {:keys [board]}]
  (app board))


