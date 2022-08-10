(ns amble-client.move-helpers
  "Shared code between `move-remote` and `move-local`"
  (:require [amble-client.interpolate-function :refer [interpolate-function]]))

;; (defn set-point! [app-atom, player-id, player-piece-index, x, y]
  ;; (swap! app-atom assoc-in [:player-pieces player-id (js/parseInt player-piece-index)] [x, y]))

(defn end-move-at-point! [app-atom, {:keys [player-id, player-piece-index, x, y, move]}]
  (assert (keyword? player-id)
          "`player-id` is not a keyword")
  (let [[last-x, last-y] (last move)]
    (interpolate-function (fn [{:keys [x, y]}]
                            (swap! app-atom assoc-in [:player-pieces player-id (js/parseInt player-piece-index)] [x, y]))
                          :x1 last-x
                          :y1 last-y
                          :x2 x
                          :y2 y)))
