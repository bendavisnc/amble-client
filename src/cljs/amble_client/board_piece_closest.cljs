(ns amble-client.board-piece-closest
  "Figures out which piece is closest, given some x y value."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async :refer [go]]))

(def board-pieces-atom (atom nil))

(defn board-piece-closest [x, y]
  (first (sort-by (fn [board-piece]
                    (let [xxx (- (:x board-piece) x)
                          yyy (- (:y board-piece) y)]
                      (Math/sqrt (+ (* xxx xxx)
                                    (* yyy yyy)))))
                  (deref board-pieces-atom))))

(defmethod ig/init-key :amble/board-piece-closest [_ {:keys [app-atom, app-ready-chan]}]
  (go
    (async/<! app-ready-chan)
    (reset! board-pieces-atom (:board-pieces (deref app-atom))))
  board-piece-closest)



