(ns amble-client.board
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(def board [:svg {:id "board" "viewBox" "0 0 1 1"}])

(defmethod ig/init-key :amble/board [_ {:keys [board-pieces, player-pieces]}]
  (go
    (conj board
          (concat (async/<! board-pieces)
                  (async/<! player-pieces)))))
