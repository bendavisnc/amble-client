(ns amble-client.app
  (:require [integrant.core :as ig]))

(def app-id "amble")

(defn app [board]
  (fn []
    [:div {:id app-id}
          [board]]))

(defn portrait-mode? []
  (not (= -1
          (.indexOf (.-type (.-orientation js/screen))
                    "ortrait"))))

(defn orientation []
  (if (portrait-mode?) :portrait :landscape))

(defmethod ig/init-key :amble/app [_ {:keys [app-atom, board]}]
  ;; Make the app responsive by listening to the following and making global state reflect the change.
  (.addListener (.matchMedia js/window
                             "(orientation: portrait)")
                (fn [m]
                  (swap! app-atom
                         assoc-in
                         [:app :orientation]
                         (if (.-matches m) :portrait :landscape))))

  (swap! app-atom assoc-in [:app :orientation] (orientation))
  (app board))