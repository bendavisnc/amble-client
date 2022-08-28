(ns amble-client.main-menu
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [clojure.string]
            [goog.string :refer [unescapeEntities]]
            [cljsjs.react])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

;; (def app-atom-chan (async/chan))
;; (def app-orientation-chan (async/chan))

(def board ::board)
(def moves ::moves)
(def settings ::settings)

(def menu-items [board, moves, settings])

(def menu-item-selected-atom (atom nil))

(def app-orientation-atom (atom nil))

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
    ;;         (str "Couldn't find highlight-item in dom for menu-item, " menu-item "."))
    hi))

(defn set-selected! [app-atom, menu-item]
  (swap! app-atom assoc-in [:main-menu :menu-item-selected]
         menu-item))

(defmethod content :default [thiz, app-atom]
  [:div {:class (str "menu-item "
                     (name thiz))
         :on-click (fn [_]
                     (cljs.core/reset! menu-item-selected-atom thiz)
                     (set-selected! app-atom, thiz))
         :on-mouse-enter (fn [_] (set-selected! app-atom, thiz))
         :on-mouse-leave (fn [_] (set-selected! app-atom (deref menu-item-selected-atom)))}

   (name thiz)])

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
            

;; (defn highlight-container-style [app-atom]
;;   (let [offsets (offsets app-atom)]
;;     (if (empty? offsets)
;;       ""
;;       (let [
;;             offsets-with-menu-items (map vector offsets menu-items)]
;;         (clojure.string/join "\n"
;;                              (for [[offset, menu-item] offsets-with-menu-items]
;;                               ;;  (str "body #amble #main-menu #highlight-container .highlight-item.")
;;                                (str ".highlight-item."
;;                                     (selected-class-name menu-item)
;;                                     " { "
;;                                     (if (portrait-mode? app-atom)
;;                                       "left"
;;                                       "top")
;;                                     ": "
;;                                     offset
;;                                     "px;}")))))))

(defn highlight-container [app-atom]
  [:div {:id "highlight-container"}
        ;;  "STYLE" (highlight-container-style app-atom)}
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
    ;; (async/put! app-orientation-chan (get-in (deref app-atom) [:app :orientation]))
    (println "resetting thing")
    ;; (println (str (deref app-atom)))
    (println (get-in (deref app-atom) [:app :orientation])) 
    (cljs.core/reset! app-orientation-atom (get-in (deref app-atom) [:app :orientation]))
    [:div {:id "main-menu"}
     [highlight-container app-atom]
     [:div {:id "menu-items-container" :data-mode (name (get-in (deref app-atom) [:app :orientation]))}
      (for [menu-item menu-items]
        ^{:key (name menu-item)}
        [:<>
         (content menu-item, app-atom)])]]))


(defn update-styles! [app-atom]
  (let [offsets (offsets app-atom)]
    (when-not (empty? offsets)
       (let [
             offsets-with-menu-items (map vector offsets menu-items)
             stylesheet (aget (.-styleSheets js/document)
                              0)]
         (println (str "Setting up css rules for main menu based off of offset values, \"" 
                         offsets
                         "\", that come from the css flex declarations."))
        (doall
          (for [[offset, menu-item] offsets-with-menu-items
                :let [s
                      (str "body #amble #main-menu #highlight-container .highlight-item."
                           (selected-class-name menu-item)
                          ;;  " { top: "
                           " { "
                           (if (portrait-mode? app-atom)
                             "left"
                             "top")
                           ": "
                           offset
                           "px;}")]] 
            (do
              (println (str "Setting new style rule, " s))                      
              (try
                (.deleteRule stylesheet 0)
                (catch js/Object e
                  (do (println "Problem with managing styles dynamically.")
                      (println e))))
              (.insertRule stylesheet s 0))))))))


;; (go-loop [app-atom (async/<! app-atom-chan)
;;           orientation-prior nil]
;;   (let [o (async/<! app-orientation-chan)
;;         _ (println (str "wut, " o))]    
;;     (if (= o orientation-prior)
;;       (recur app-atom o)
;;       (do (update-styles! app-atom)
;;           (recur app-atom o)))))

(defmethod ig/init-key :amble/main-menu [_ {:keys [app-atom, app-ready-chan]}]
  ;; (async/put! app-atom-chan app-atom)
  (swap! app-atom assoc-in [:main-menu :menu-item-selected] board)
  (cljs.core/reset! menu-item-selected-atom board)
  (add-watch app-orientation-atom nil (fn [_, _, orientation-prior, orientation-now]
                                        (println [orientation-prior, orientation-now])
                                        (if (= orientation-now, orientation-prior)
                                          (println "Orientation change is ignored, no real change occurred.")
                                          (update-styles! app-atom))))
  (async/go
    (async/<! app-ready-chan)
    (cljs.core/reset! app-orientation-atom nil) 
    (cljs.core/reset! app-orientation-atom (get-in (deref app-atom)
                                                   [:app :orientation])))

  (main-menu app-atom))


