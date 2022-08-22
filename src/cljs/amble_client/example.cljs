(ns amble-client.example
  (:require [integrant.core :as ig]
            ["react-transition-group" :refer [TransitionGroup CSSTransition]]
            [cljsjs.react]))

;; taken from
;;   https://github.com/reagent-project/reagent-cookbook/blob/master/recipes/ReactCSSTransitionGroup/README.md#step-5-create-the-initial-app-state

(def style
  "li {
  background-color: #44ee22; padding: 10px; margin: 1px; width: 150px;
  border-radius: 5px;
  font-size: 24px;
  text-align: center;
  list-style: none;
  color: #fff;
  height: 2em;
  line-height: 2em;
  padding: 0 0.5em;
  overflow: hidden;
  }

  .foo-enter {
  height: 0;
  transition: height 0.07s ease-in;
  }

  .foo-leave, .foo-exit {
  height: 0;
  transition: height 2.27s linear;
  }

  .foo-enter-active {
  height: 2em;
  opacity: 1;
  }")

;; (def css-transition-group 
;;   (try
;;     (reagent/adapt-react-class js/React.addons.CSSTransitionGroup)
;;     (catch js/Error e
;;       (println "welp")
;;       (println e)))) 
;

;; (defn css-transition-group [] 
;;   (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))
;;  ;; (try
;;  ;;   (reagent/adapt-react-class js/React.addons.CSSTransitionGroup)
;;  ;;   (catch js/Error e
;;  ;;     (println "wuuuut")
;;  ;;     (println e))))

(defn add-item-builder [app-state]
  (fn []
    (let [items (:items @app-state)]
      (swap! app-state update-in [:items-counter] inc)
      (swap! app-state assoc :items (conj items (:items-counter @app-state))))))

(defn delete-item-builder [app-state]
  (fn []
    (let [items (:items @app-state)]
      (swap! app-state assoc :items (vec (butlast items))))))



(defn home-builder [app-state]
  (fn []
    [:div
     [:div (str "Total list items to date:  " (:items-counter @app-state))]
     [:button {:on-click (add-item-builder app-state)} "add"]
     [:button {:on-click (delete-item-builder app-state)} "delete"]
     [:style style]
     [:> TransitionGroup {:component "ul"}
       (for [[i, x] 
             (map-indexed vector (:items @app-state))]
         [:> CSSTransition
           {:key i 
            :class-names "foo"
            :timeout 500}
           [:li (str "List Item " x)]])]]))

;; (defn home-builder [app-state]
;;   (fn []
;;     [:div
;;      [:div (str "Total list items to date:  " "7")]]))


(defmethod ig/init-key :amble/example [_ {:keys [app-atom]}]
  (try (do
         (swap! app-atom assoc :items [])
         (swap! app-atom assoc :items-counter 0)
         (home-builder app-atom))
       (catch js/Object e
         (do 
           (.log js/console "welllp")
           (println e))))) 
      


