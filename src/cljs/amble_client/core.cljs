(ns amble-client.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

(defn wut []
  [:div {:class neat :id neato} "hi"])

(defn init![]
  (rdom/render [wut] (.getElementById js/document "app")))