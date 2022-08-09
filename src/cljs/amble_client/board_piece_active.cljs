(ns amble-client.board-piece-active
  "Highlights whichever board piece is closest to the active player piece."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))
(def move-xy-chan (async/chan))
(def board-piece-closest-chan (async/chan))

(go-loop [app-atom (async/<! app-atom-chan)
          board-piece-closest (async/<! board-piece-closest-chan)]
  (let [_ (println "hiiiiiiii")
        {:keys [x,y]} (async/<! move-xy-chan)
        board-piece (board-piece-closest x, y)]
    (println "Updating active board piece.")
    (swap! app-atom assoc-in [:board-pieces (:index board-piece)] (assoc board-piece :is-active? true))
    (recur app-atom, board-piece-closest)))   

(defmethod ig/init-key :amble/board-piece-active [_ {:keys [app-atom, board-piece-closest, move-xy-chan, app-ready-chan]}]
  (println "whhhhhhhhhhhhhht")
  (async/pipe move-xy-chan amble-client.board-piece-active/move-xy-chan)
  (go
    (async/<! app-ready-chan)
    (async/>! app-atom-chan app-atom)
    (async/>! board-piece-closest-chan board-piece-closest))
  nil)