(ns amble-client.main-menu
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [goog.string :as gstring :refer [unescapeEntities]]
            [goog.string.format]
            [clojure.string]))

(def app-orientation-chan (async/chan (async/dropping-buffer 1)
                                      (dedupe)))

(def board :board)
(def moves :moves)
(def settings :settings)

(def menu-items [board, moves, settings])

(def menu-item-selected-atom (atom nil))

(defn selected-class-name [menu-item]
  (menu-item {board "first-selected", moves "second-selected", settings "third-selected"}))

(defn highlight-item [menu-item]
  (if-let [item
           (.getElementById js/document (str (name menu-item)
                                             "-highlight-item"))]
    item
    (println (str "Couldn't find highlight-item in dom for menu-item, " menu-item "."))))

(defn set-selected! [app-atom, menu-item]
  (swap! app-atom assoc-in [:main-menu :menu-item-selected]
         menu-item))

(defn content [menu-item, app-atom]
  [:div {:class (str "menu-item "
                     (name menu-item))
         :on-click (fn [_]
                     (cljs.core/reset! menu-item-selected-atom menu-item)
                     (set-selected! app-atom, menu-item))
         :on-mouse-enter (fn [_] (set-selected! app-atom, menu-item))
         :on-mouse-leave (fn [_] (set-selected! app-atom (deref menu-item-selected-atom)))}

   (name menu-item)])

(defn portrait-mode? [app-atom]
  (= :portrait (get-in (deref app-atom)
                       [:app :orientation])))

(defn offset [app-atom, menu-item]
  (when-let [item
             (highlight-item menu-item)]
    (if (portrait-mode? app-atom)
      (.-offsetLeft item)
      (.-offsetTop item))))

(defn offsets [app-atom]
  (mapcat (fn [menu-item]
            (if-let [offset (offset app-atom menu-item)]
              [offset]
              []))
          menu-items))

(defn highlight-container [app-atom]
  [:div {:id "highlight-container"}
   [:<>
    (doall
     (for [[i, menu-item]
           (map-indexed vector menu-items)
           :let [item-name (str (name menu-item)
                                "-highlight-item")]]

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
    (async/put! app-orientation-chan
                (get-in (deref app-atom) [:app :orientation]))
    [:div {:id "main-menu"}
     [highlight-container app-atom]
     [:div {:id "menu-items-container" :data-mode (name (get-in (deref app-atom) [:app :orientation]))}
      (for [menu-item menu-items]
        ^{:key (name menu-item)}
        [:<>
         (content menu-item, app-atom)])]]))

(defn update-styles! [app-atom]
  (let [offsets (offsets app-atom)]
    (if (empty? offsets)
      (throw (new js/Error "`offsets` was passed as empty for updating style info."))
      (let [orientation (get-in (deref app-atom)
                                [:app :orientation])
            offsets-with-menu-items (map vector offsets menu-items)
            stylesheet (aget (.-styleSheets js/document)
                             0)]
        (println (str "Setting up css rules for main menu based off of offset values, \""
                      offsets
                      "\", that come from the css flex declarations."))
        (doall
         (for [[offset, menu-item] offsets-with-menu-items
               :let [s
                     (gstring/format "@media (orientation: %s) {body #amble #main-menu #highlight-container .highlight-item.%s { %s: %spx;}"
                                     (name orientation)
                                     (selected-class-name menu-item)
                                     (orientation {:portrait "left", :landscape "top"})
                                     offset)]]
           (do
             (println (str "Setting new style rule, " s))
             (.insertRule stylesheet s 0))))))))

(defmethod ig/init-key :amble/main-menu [_ {:keys [app-atom, app-ready-chan]}]
  (swap! app-atom assoc-in [:main-menu :menu-item-selected] board)
  (cljs.core/reset! menu-item-selected-atom board)
  ;; `update-styles!` is called once per orientation change.
  (async/go
    (async/<! app-ready-chan)
    (async/<! app-orientation-chan)
    (update-styles! app-atom)
    (async/<! app-orientation-chan)
    (update-styles! app-atom))
  (main-menu app-atom))


