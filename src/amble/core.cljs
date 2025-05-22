(ns amble.core
  (:require
   [amble.view :as view]
   [amble.server.main]
   [amble.server.server]
   [amble.models.amble :as model]
   [re-frame.core :as re-frame]
   [reagent.dom.client :as reagent-dom]))

(def app (js/document.getElementById "app"))

(defn- mount-app []
  (reagent-dom/render
    (reagent-dom/create-root app)
    [view/ui]))

(defn- init []
  (mount-app)
  (re-frame/dispatch-sync [::model/initialize]))

(defn on-figwheel-reload []
  (mount-app))

(.addEventListener js/document "DOMContentLoaded" init)
