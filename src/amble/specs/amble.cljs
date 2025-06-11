(ns amble.specs.amble
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::player #{:player-one, :player-two, :player-three
                  :player-four, :player-five, :player-six})

(s/def ::player-selected ::player)

(s/def ::position
  (s/coll-of (s/coll-of number? :count 2) :count 10))

(s/def ::players
  (s/map-of ::player
            (s/keys :req-un [::position])))

(s/def ::is-active? boolean?)
(s/def ::x number?)
(s/def ::y number?)
(s/def ::index nat-int?)

(s/def ::piece
  (s/keys :req-un [::is-active?, ::x, ::y, ::index]))

(s/def ::pieces (s/coll-of ::piece))

(s/def ::board
  (s/keys :req-un [::pieces]))





(s/def ::component
  (s/keys :req-un [::player-selected, ::players, ::board]))
