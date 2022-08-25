(ns amble-client.main-menu
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [goog.string :refer [unescapeEntities]]
            ["react-transition-group" :refer [TransitionGroup CSSTransition]]
            [cljsjs.react])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

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
  "")

(defn highlight-item [menu-item]
  (let [hi
        (.getElementById js/document (str (name menu-item)
                                          "-highlight-item"))]
    ;; (assert (not (nil? hi))
            ;; (str "Couldn't find highlight-item in dom for menu-item, " menu-item "."))]
    hi))

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

(defn offset [menu-item]
  (.-offsetLeft (highlight-item menu-item)))

(defn update-styles! []
  (let [offsets (map offset menu-items) 
        offsets-with-menu-items (map vector offsets menu-items)
        stylesheet (aget (.-styleSheets js/document)
                         0)]
    (println (str "Setting up main menu based of offset values, \"" 
                  offsets
                  "\", that come from the css flex declarations."))
    (doall
      (for [[offset, menu-item] offsets-with-menu-items
            :let [s
                  (str "body #amble #main-menu #highlight-container .highlight-item."
                       (selected-class-name menu-item)
                      ;;  " { top: "
                       " { left: "
                       offset
                       "px;}")]] 
        (.insertRule stylesheet s 0)))
    (.addEventListener (.-body js/document)
                       "touchstart"       
                       (fn [e] (.-preventDefault e)))))       

(defmethod ig/init-key :amble/main-menu [_ {:keys [app-atom, app-ready-chan]}]
  (go
    (async/<! app-ready-chan)
    (update-styles!))
  (swap! app-atom assoc-in [:main-menu :menu-item-selected] board)
  (reset! menu-item-selected-atom board)
  (main-menu app-atom))


