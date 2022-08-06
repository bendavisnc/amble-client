(ns amble-client.move-end
  "Provides action for placing piece after the end of a move."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.environment :refer [environment]]
            [haslett.client :as haslett-client])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))

(def move-chan (async/chan))

;; Listens for moves and sends pieces back to the board at the end of a move.
;; Targets both local and remote moves.
(go-loop [app-atom (async/<! app-atom-chan)]
  (let [{:keys [player-id, player-piece-index, x, y] :as move-latest} (async/<! move-chan)]
    (println "hey neat guys")
    (println move-latest)
    (recur app-atom)))

(defmethod ig/init-key :amble/move-end [_ {:keys [app-atom, move-chan]}]
  (async/pipe move-chan amble-client.move-end/move-chan)
  (js/setTimeout (fn [& args]
                   (async/put! app-atom-chan app-atom))
                 1000) 
  nil)


