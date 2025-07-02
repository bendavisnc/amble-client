(ns amble.amble)

(defn index-to-player
  "Maps an integer index to a player keyword."
  [i]
  (case i
    0 :player-one
    1 :player-two
    2 :player-three
    3 :player-four
    4 :player-five
    5 :player-six
    nil))

(defn player-to-index
  "Maps a player keyword to an integer index."
  [player]
  (case player
    :player-one 0
    :player-two 1
    :player-three 2
    :player-four 3
    :player-five 4
    :player-six 5
    nil))
