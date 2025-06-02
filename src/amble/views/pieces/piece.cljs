(ns amble.views.pieces.piece)

(def piece-size 0.023)

(defn piece [& {:keys [x, y, size, id, class]}]
  [:circle {:cx x
            :cy y
            :r size
            :key id
            :id id
            :class class}])
