(ns amble-client.board
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(def board [:svg {:id "board" "viewBox" "0 0 1 1"}])

(defmethod ig/init-key :amble/board [_ {:keys [board-pieces]}]
  (go
    (conj board
          (async/<! board-pieces))))
