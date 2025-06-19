(ns amble.specs.amble
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::player #{:player-one :player-two :player-three
                  :player-four :player-five :player-six})

(s/def ::player-selected ::player)

(s/def ::position
  (s/coll-of (s/coll-of number? :count 2) :count 10))

(s/def ::client-id
  string?)

(s/def ::moves-made
  (s/coll-of ::client-id))

(s/def ::players
  (s/map-of
    ::player
    (s/and
      (s/keys :req-un [::position, ::moves-made])
      #(= (set (keys %)) #{:position, :moves-made}))))

(s/def ::is-active? boolean?)
(s/def ::x number?)
(s/def ::y number?)
(s/def ::index nat-int?)

(s/def ::piece
  (s/cat :x ::x :y ::y))

(s/def ::pieces
  (s/coll-of ::piece))

(s/def ::occupied
  (s/coll-of pos-int?))

(s/def ::board
  (s/and
    (s/keys :req-un [::pieces, ::occupied])
    #(= (set (keys %)) #{:pieces, :occupied})))

(s/def ::landing-piece
  (s/nilable any?)) ;; todo

(s/def ::component
  (s/and
    (s/keys :req-un [::player-selected ::players ::board, ::landing-piece])
    #(= (set (keys %)) #{:player-selected :players :board :landing-piece})))

(comment (s/explain-str ::moves-made #{}))
