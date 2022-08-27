(ns amble-client.app
  (:require [integrant.core :as ig]))

(def app-id "amble")

(defn app [app-atom, board, main-menu]
  (fn []
    [:div {:id app-id}
      [main-menu (get-in (deref app-atom)
                         [:app, :orientation])]
      [board]]))

(defn portrait-mode? []
  (not (= -1
          (.indexOf (.-type (.-orientation js/screen))
                    "ortrait"))))

(defn orientation []
  (if (portrait-mode?) :portrait :landscape))

(defmethod ig/init-key :amble/app [_ {:keys [app-atom, board, main-menu]}]
  ;; (.addEventListener (.-orientation js/screen)
  ;;                    "change"
  ;;                    (fn [_]
  ;;                      (println "neattttt?")
  ;;                      (swap! app-atom assoc-in [:app :orientation] (orientation))))
 
  (.addListener (.matchMedia js/window
                             "(orientation: portrait)")
                (fn [_]
                  (swap! app-atom assoc-in [:app :orientation] (orientation))))

  (swap! app-atom assoc-in [:app :orientation] (orientation))
  (app app-atom, board, main-menu))

;; window
;; .matchMedia('(orientation: portrait)')
;;     .addListener(function (m) {
;;                                if (m.matches) {
;;                                                // portrait}
;;                                else {
;;                                      // landscape}})
        
;;     ;

