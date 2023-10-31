(ns amble-client.moves
  "Keeps track of a game's moves."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async])
  (:require-macros [cljs.core.async :refer [go-loop]]))

(def move-chan (async/chan))
(def app-atom-chan (async/chan))

(go-loop [app-atom (async/<! app-atom-chan)]
  (let [move (async/<! move-chan)]
    (println (str "New move acknowledged, move " (count (:moves @app-atom))))
    (swap! app-atom
           update-in
           [:moves]
           conj
           move)
    (recur app-atom))) 

(defmethod ig/init-key :amble/moves [_ {:keys [app-atom, move-remote-chan]}]
  (async/put! app-atom-chan app-atom)
  (async/pipe move-remote-chan
              amble-client.moves/move-chan)
  nil)


