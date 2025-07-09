(ns amble.views.pieces.board
  (:require
   [amble.views.pieces.piece :as piece]))
(def classname "board-piece")

(defn pieces [{:keys [pieces-seq, active-index, occupied]}]
  [:<>
   (for [[index, [x, y]] (map-indexed vector pieces-seq)
         :let [is-active? (= index active-index)
               is-occupied? (occupied index)]]
     (piece/piece :x x
                  :y y
                  ;; This class is just for debugging, but still, this addresses seeing the debug color 
                  ;; underneath a regular piece. 
                  :size (* piece/piece-size
                           (if is-occupied? 0.9 1))
                  :id (str classname "-" index)
                  :class [classname
                          (if is-active? " active" "")
                          (if is-occupied? " occupied" "")]
                  :extra-opts {:data-index index}))])
