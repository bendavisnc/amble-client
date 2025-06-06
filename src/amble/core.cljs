(ns amble.core
  (:require
   [amble.models.amble :as model]
   [amble.models.dev]
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
  (re-frame/dispatch-sync [::model/initialize]))

(defn on-figwheel-reload []
  (mount-app))

(.addEventListener js/document "DOMContentLoaded" init)
