(ns amble.views.controls
  (:require
   [amble.models.controls :as models]
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(def controls [{:id ::undo
                :icon "<"
                :disabled :undo-disabled
                :on-click #(re-frame/dispatch [::models/on-control-undo])}
               {:id ::redo
                :icon ">"
                :disabled :redo-disabled
                :on-click #(re-frame/dispatch [::models/on-control-redo])}
               {:id ::rotate
                :icon "↻"
                :on-click #(re-frame/dispatch [::models/on-control-rotate])}])

(defn- control-button [disabled-map {:keys [id icon on-click disabled]}]
  (let [is-disabled? (some-> disabled disabled-map)]
    [:button.control (merge {:id (name id)
                             :type "button"
                             :title (if is-disabled?
                                      (gstring/format "%s (disabled)" (name id))
                                      (name id))
                             :on-click on-click}
                       (when is-disabled?
                         {:disabled true}))
     [:span.icon icon]]))

(defn component [disabled-map]
  [:div#controls
   (for [control controls]
     ^{:key control}
     [control-button disabled-map control])])

;; (comment (gstring/format "nice %s" :wut))
