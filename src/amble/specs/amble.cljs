(ns amble.specs.amble
  (:require
   [clojure.spec.alpha :as s]))

(s/def ::player #{:player-one, :player-two})

(s/def ::player-selected ::player)


(s/def ::players #{2})

(s/def ::component
  (s/keys :req-un [::player-selected, ::players]))
