(ns amble.views.pieces.board)

(def classname "board-piece")
(def piece-size 0.023)

(defn- piece [& {:keys [x, y, size, id, class]}]
  [:circle {:cx x
            :cy y
            :r size
            :key id
            :id id
            :class class}])

(defn pieces [pieces-seq]
  [:<>
   (for [{:keys [x, y, is-active?, index]} pieces-seq]
     (piece :x x
            :y y
            :size piece-size
            :id (str classname "-" index)
            :class (str classname
                        (if is-active? " active" ""))))])
