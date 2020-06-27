(ns amble-client.markup.core
  (:require [amble-client.markup.state :as markup-state]))

(defn app-markup-error []
  [:div {:id "badnews" :title (first (markup-state/get :errors))}
   "bad news"])

(defn piece-markup [& {:keys [x, y, size, class, index] :as piece}]
  [:circle {:cx x,
            :cy y
            :r size
            :key (str class
                      [x, y])
            :class class
            :xboardindex index
            :on-click (fn []
                        (println "come back to")
                        (println piece))}])

(def player-classes
  ["player-one"
   "player-two"
   "player-three"
   "player-four"
   "player-five"
   "player-six"])


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
                                     :size (* 0.98 piece-size) ;; Cheap way to prevent seeing a placeholder piece when a normal piece is sitting above.
                                     :class "designatee"
                                     :index i))
                     (markup-state/get :placement :designatee))
                   (apply concat
                          (map-indexed
                            (fn [i, player-class]
                              (let [player-index (inc i)
                                    player-coords
                                    (markup-state/get :placement :player player-index)]

                                (map
                                  (fn [{:keys [index, coord]}]
                                    (let [[x, y] coord]
                                      (piece-markup :x x :y y :size piece-size :class player-class :index index)))
                                  player-coords)))
                            player-classes))))]))




(defn app-markup []
  [:div {:id "amble"} (board-markup)])

(defn app-markup-error-checked []
  (if (first (markup-state/get :errors))
    [app-markup-error]
    [app-markup]))
