(ns amble-client.board
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(defn init [board-pieces, player-pieces]
  9)
  ;; (fn []
  ;;   [:svg {:id "board" "viewBox" "0 0 1 1"}
  ;;    [board-pieces]]))
  ;;   ;;  [player-pieces]]))


(defmethod ig/init-key :amble/board [_ {:keys [board-pieces, player-pieces]}]
  (println "real neat")
  (println [board-pieces, player-pieces])
  (init board-pieces, player-pieces))


