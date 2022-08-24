(ns amble-client.main-menu
  (:require [integrant.core :as ig]
            [goog.string :refer [unescapeEntities]]
            ["react-transition-group" :refer [TransitionGroup CSSTransition]]
            [cljsjs.react]))

(def board ::board)
(def moves ::moves)
(def settings ::settings)

(def menu-items [board, moves, settings])

(def menu-item-selected-atom (atom nil))

(defmulti content (fn [menu-item, _]
                    menu-item))

(defmulti selected-class-name identity)

(defmethod selected-class-name board [_]
  "first-selected")

(defmethod selected-class-name moves [_]
  "second-selected")

(defmethod selected-class-name settings [_]
  "third-selected")

(defmethod selected-class-name :default [menu-item-unknown]
  (println (str "weird, " menu-item-unknown))
  "")


;; (defmethod content board [_]
;;   [:div "board"])

;; (defmethod content moves [_]
;;   [:div "moves"])

;; (defmethod content settings [_]
;;   [:div "settings"])

(defn set-selected! [app-atom, menu-item]
  (swap! app-atom assoc-in [:main-menu :menu-item-selected] 
                           menu-item))
 
(defmethod content :default [thiz, app-atom]
  [:div {:class (str "menu-item "
                     (name thiz))
         :on-click (fn [_] 
                     (reset! menu-item-selected-atom thiz) 
                     (set-selected! app-atom, thiz))
         :on-mouse-enter (fn [_] (set-selected! app-atom, thiz))
         :on-mouse-leave (fn [_] (set-selected! app-atom (deref menu-item-selected-atom)))}
 
   (name thiz)])

(defn on-enter []
  (println "well cool beans"))

(defn highlight-container [app-atom]
  [:div {:id "highlight-container"}
        [:<>
          (doall
            (for [[i, menu-item]
                  (map-indexed vector menu-items)
                  :let [item-name (str (name menu-item)
                                       "-highlight-item")
                        is-selected?
                        (get-in (deref app-atom)
                                [:main-menu menu-item])]]

              (if (zero? i)
                ^{:key (name menu-item)}
                [:div {:id (str item-name)
                       :class (str "highlight-item"
                                   " "
                                   (selected-class-name (:menu-item-selected (:main-menu (deref app-atom)))))}
                  (unescapeEntities "&nbsp;")]
                ^{:key (name menu-item)}
                [:div {:id (str item-name)
                           :class (str "highlight-item"
                                        " "
                                       "place-holder")}
                  (unescapeEntities "&nbsp;")])))]])


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
  (swap! app-atom assoc-in [:main-menu :menu-item-selected] board)
  (reset! menu-item-selected-atom board)
  (main-menu app-atom))


