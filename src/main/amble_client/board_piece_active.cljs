(ns amble-client.board-piece-active
  "Highlights whichever board piece is closest to the active player piece."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))
(def move-xy-chan (async/chan))
(def board-piece-closest-chan (async/chan))

(defn set-active! [app-atom, board-piece, is-active?]
  (swap! app-atom assoc-in [:board-pieces 
                            (:index board-piece) 
                            :is-active?] 
                           is-active?))

(defn activate! [app-atom, board-piece]
  (set-active! app-atom board-piece true))

(defn deactivate! [app-atom, board-piece]
  (set-active! app-atom board-piece false))

(go-loop [app-atom (async/<! app-atom-chan)
          board-piece-closest (async/<! board-piece-closest-chan)]
  (let [{:keys [x,y]} (async/<! move-xy-chan)
        board-piece (board-piece-closest x, y)]
    (when-let [board-piece-active-last
               (first (filter :is-active?
                              (:board-pieces (deref app-atom))))]
      (deactivate! app-atom board-piece-active-last))

    (activate! app-atom board-piece)
    (recur app-atom, board-piece-closest)))

(defmethod ig/init-key :amble/board-piece-active [_ {:keys [app-atom, board-piece-closest, move-xy-chan, app-ready-chan]}]
  (async/pipe move-xy-chan amble-client.board-piece-active/move-xy-chan)
  (go
    (async/<! app-ready-chan)
    (async/>! app-atom-chan app-atom)
    (async/>! board-piece-closest-chan board-piece-closest))
  nil)