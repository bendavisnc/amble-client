(ns amble.views.pieces.board
  (:require
   [amble.views.pieces.piece :as piece]))
(def classname "board-piece")

;; (def dispatch-map
  ;;  {"mousemove" ::board/move-update})

(defn pieces [{:keys [pieces-seq, active-index]}]
  [:<>
   (for [[index, [x, y]] (map-indexed vector pieces-seq)
         :let [is-active? (= index active-index)]]
     (piece/piece :x x
                  :y y
                  :size piece/piece-size
                  :id (str classname "-" index)
                  :class (str classname (if is-active? " active" ""))))])
