(ns amble-client.moves
  (:require [integrant.core :as ig]))

(defn moves []
  (fn []
    [:div [:span "moves, coming soon"]]))

(defmethod ig/init-key :amble/moves [_ {:keys []}]
  (moves))


