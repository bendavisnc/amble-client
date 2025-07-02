(ns amble.views.controls
  (:require
   [amble.models.controls :as models]
   [re-frame.core :as re-frame]))

(def controls {::rotate {:id "rotate"
                         :symbol-icon "↻"
                         :on-click #(re-frame/dispatch [::models/on-control :rotate])}})

(defn- control [{:keys [id symbol-icon on-click]}]
  [:button.control {:id id
                    :type "button"
                    :on-click on-click}
   [:span.symbol-icon symbol-icon]])

(defn component []
  [:div#controls
   [control (controls ::rotate)]])
