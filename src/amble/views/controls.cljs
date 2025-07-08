(ns amble.views.controls
  (:require
   [amble.models.controls :as models]
   [re-frame.core :as re-frame]))

(def controls [{:id ::rotate
                :icon "↻"
                :on-click #(re-frame/dispatch [::models/on-control-rotate])}
               {:id ::redo
                :icon ">"
                :on-click #(re-frame/dispatch [::models/on-control-redo])}
               {:id ::undo
                :icon "<"
                :on-click #(re-frame/dispatch [::models/on-control-undo])}])

(defn- control-button [{:keys [id icon on-click]}]
  [:button.control {:id (name id)
                    :type "button"
                    :title (name id)
                    :on-click on-click}
   [:span.icon icon]])

(defn component []
  [:div#controls
   (for [control controls]
     ^{:key control}
     [control-button control])])
