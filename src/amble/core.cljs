(ns amble.core
  (:require
   [amble.dev]
   [amble.models.amble :as model]
   [amble.models.hash-params :as hash-params]
   [amble.server.async.main]
   [amble.server.async.server]
   [amble.server.main]
   [amble.server.server]
   [amble.view :as view]
   [re-frame.core :as re-frame]
   [reagent.dom.client :as reagent-dom]))

(def app (js/document.getElementById "app"))

(defn- mount-app []
  (reagent-dom/render
    (reagent-dom/create-root app)
    [view/ui]))

(defn- init []
  (mount-app)
  (re-frame/dispatch-sync [::model/initialize])
  (.addEventListener js/window "hashchange"
                     (fn [_]
                       (re-frame/dispatch [::hash-params/update])))
  nil)

(defn on-figwheel-reload []
  (mount-app))

(.addEventListener js/document "DOMContentLoaded" init)
