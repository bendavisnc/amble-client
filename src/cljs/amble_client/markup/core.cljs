(ns amble-client.markup.core
  (:require [amble-client.state :as markup-state]
            [clojure.string :as s]))

(def placeholder-class "designatee")
(def player-prefix "player")
(def player-classes
  (map #(str player-prefix
             "-"
             %)
       ["one"
        "two"
        "three"
        "four"
        "five"
        "six"]))

(defn app-markup-error []
  [:div {:id "badnews" :title (first (markup-state/get :errors))}
   "bad news"])

(defn piece-markup [& {:keys [x, y, size, class, i] :as piece}]
  (let [
        unique-key (str class
                        (or i
                            [x, y]))]
    [:circle {:cx       x,
              :cy       y
              :r        size
              :key      unique-key
              :id       unique-key
              :data-i      i
              :class    [class
                         (if (not= placeholder-class
                                   class)
                           "player")]}]))

(defn board-markup []
  (let [piece-size
        0.023]
    [:svg {:id "board" "viewBox" "0 0 1 1"}
     (doall
       (concat
         (map-indexed
           (fn [i, [x, y]]
             (piece-markup :x x
                           :y y
                           :size (* 0.98 piece-size)        ;; Cheap way to prevent seeing a placeholder piece when a normal piece is sitting above.
                           :class placeholder-class
                           :index i))
           (markup-state/get :placement :designatee))
         (apply concat
                (map-indexed
                  (fn [i, player-class]
                    (let [player-index i
                          player-coords (markup-state/get :placement :player player-index)]
                      (map-indexed
                        (fn [player-piece-index [x, y]]
                          (piece-markup :x x :y y :size piece-size :class player-class :i player-piece-index))
                        player-coords)))
                  player-classes))))]))

(defn app-markup []
  [:div {:id "amble"} (board-markup)])

(defn app-markup-error-checked []
  (if (first (markup-state/get :errors))
    [app-markup-error]
    [app-markup]))
