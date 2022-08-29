(ns amble-client.app
  (:require [integrant.core :as ig]))

(def app-id "amble")

(defn app [app-atom, main-menu, board, moves, settings]
  (fn []
    (let [menu-item-selected (get-in (deref app-atom)
                                     [:main-menu :menu-item-selected])
          content-selected (menu-item-selected {:board board, :moves moves, :settings settings})]
      [:div {:id app-id}
        [main-menu]
        [content-selected]])))

(defn portrait-mode? []
  (not (= -1
          (.indexOf (.-type (.-orientation js/screen))
                    "ortrait"))))

(defn orientation []
  (if (portrait-mode?) :portrait :landscape))

(defmethod ig/init-key :amble/app [_ {:keys [app-atom, main-menu, board, moves, settings]}]
  ;; Make the app responsive by listening to the following and making global state reflect the change.
  (.addListener (.matchMedia js/window
                             "(orientation: portrait)")
                (fn [m]
                  (swap! app-atom
                         assoc-in
                         [:app :orientation]
                         (if (.-matches m) :portrait :landscape))))

  (swap! app-atom assoc-in [:app :orientation] (orientation))
  (app app-atom, main-menu, board, moves, settings))

;; window
;; .matchMedia('(orientation: portrait)')
;;     .addListener(function (m) {
;;                                if (m.matches) {
;;                                                // portrait}
;;                                else {
;;                                      // landscape}})

;;     ;

