(ns amble-client.board
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(defn board-fn [board-pieces-fn, player-pieces-fn]
  (fn []
    [:svg {:id "board" "viewBox" "0 0 1 1"}
     [board-pieces-fn]
     [player-pieces-fn]]))

(defmethod ig/init-key :amble/board [_ {:keys [board-pieces, player-pieces]}]
  (go
    (let [board-pieces-fn (async/<! board-pieces)
          player-pieces-fn (async/<! player-pieces)]
      (board-fn board-pieces-fn
                player-pieces-fn))))


