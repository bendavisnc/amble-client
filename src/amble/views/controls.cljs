(ns amble.views.controls
  (:require
   [amble.models.controls :as models]
   [re-frame.core :as re-frame]))

(def controls [{:id ::rotate
                :symbol-icon "↻"
                :on-click #(re-frame/dispatch [::models/on-control :rotate])}
               {:id ::undo
                :symbol-icon "<"
                :on-click #(re-frame/dispatch [::models/on-control :undo])}])

(defn- control [{:keys [id symbol-icon on-click]}]
  [:button.control {:id (name id)
                    :type "button"
                    :title (name id)
                    :on-click on-click}
   [:span.symbol-icon symbol-icon]])

(defn component []
  [:div#controls
   (for [c controls]
     ^{:key c}
     [control c])])
