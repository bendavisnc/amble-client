(ns amble.views.pieces.player
  (:require
   [amble.views.pieces.piece :as piece]
   [goog.string :as gstring]))

(def classname "player-piece")

(defn pieces [players]
  [:<>
   (for [player-id (keys players)
         :let [positions (get-in players [player-id :position])]]
     ^{:key player-id}
     [:<>
      (for [[index, [x, y]] (map-indexed vector positions)]
        (piece/piece :x x
                     :y y
                     :size piece/piece-size
                     :id (gstring/format "%s-%s-%s" classname (name player-id), index)
                     :class [classname, "player", (name player-id)]))])])

(comment (gstring/format "player piece %s"))
