(ns amble-client.main-menu
  (:require [integrant.core :as ig]
            [goog.string :refer [unescapeEntities]]
            ["react-transition-group" :refer [TransitionGroup CSSTransition]]
            [cljsjs.react]))

(def board ::board)
(def moves ::moves)
(def settings ::settings)

(def menu-items [board, moves, settings])

(defmulti content (fn [menu-item, _]
                    menu-item))

;; (defmethod content board [_]
;;   [:div "board"])

;; (defmethod content moves [_]
;;   [:div "moves"])

;; (defmethod content settings [_]
;;   [:div "settings"])

(defn on-click [app-atom, menu-item-selected, event]
  (println "well this is nice")
  (.log js/console menu-item-selected)
  (.log js/console event)
  (doseq [menu-item menu-items] 
    (swap! app-atom assoc-in [:main-menu menu-item] 
                             (= menu-item-selected menu-item))))
  ;; (.log js/console (str (deref app-atom))))
  

(defn menu-item-selected [app-atom]
  (get-in (deref app-atom)
          [:main-menu :item-selected]))

(defmethod content :default [thiz, app-atom]
  [:div {:class (str "menu-item "
                     (name thiz))
         :on-click (partial on-click, app-atom, thiz)}
   (name thiz)])

(defn on-enter []
  (println "well cool beans"))

(defn highlight-container [app-atom]
  [:> TransitionGroup {:component "div"
                       :id "highlight-container"}
   (doall
     (for [[i, menu-item] 
           (map-indexed vector menu-items)
           :let [item-name (str (name menu-item)
                                "-highlight-analog")
                 is-selected?
                 (get-in (deref app-atom)
                         [:main-menu menu-item])
                 _ (println (str "wtttttf " is-selected?))]]
       [:> CSSTransition {:key item-name
                          :class-names "highlight-analog"
                          :on-enter on-enter
                          :timeout 500
                          :in is-selected?}
         [:div {:id (str item-name)
                :class (str "highlight-analog"
                             (if (zero? i)
                                   ;; (menu-item-selected app-atom))
                               " actual"
                               ""))}
           (unescapeEntities "&nbsp;")]]))])

(defn main-menu [app-atom]
  (fn []
    [:div {:id "main-menu"}
     [highlight-container app-atom]
     [:div {:id "menu-items-container"}
      (for [menu-item menu-items]
        ^{:key (name menu-item)}
        [:<>
         (content menu-item, app-atom)])]]))

(defmethod ig/init-key :amble/main-menu [_ {:keys [app-atom]}]
  (main-menu app-atom))


