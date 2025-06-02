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

(s/def ::component
  (s/keys :req-un [::player-selected, ::players]))
