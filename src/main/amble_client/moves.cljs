(ns amble-client.moves
  "Keeps track of a game's moves."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.move :as move-resource])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def move-chan (async/chan))
(def app-atom-chan (async/chan))

;; Add moves to state from before client start.
(go (let [app-atom (async/<! app-atom-chan)
          game-id (:game-id @app-atom)
          moves-response (async/<! (move-resource/get! game-id))
          moves (async/<! (async/into []
                                      (async/merge
                                        (for [move-id moves-response]
                                          (move-resource/get! game-id move-id)))))
          moves (sort-by (comp (partial * -1) :id) 
                         moves)]
 
       (swap! app-atom
              assoc
              :moves
              moves)
       (println "Added " (count moves) " moves at start.")))


;; Add moves to state as they happen.
(go-loop [app-atom (async/<! app-atom-chan)]
  (let [move (async/<! move-chan)]
    (println (str "New move acknowledged, move " (count (:moves @app-atom))))
    (swap! app-atom
           update-in
           [:moves]
           conj
           move)
    (recur app-atom))) 

(defmethod ig/init-key :amble/moves [_ {:keys [app-atom, move-remote-chan, app-ready-chan]}]
  (async/pipe move-remote-chan
              amble-client.moves/move-chan)
  (go (async/<! app-ready-chan)
      (async/>! app-atom-chan app-atom)   
      (async/>! app-atom-chan app-atom))   
  nil)


