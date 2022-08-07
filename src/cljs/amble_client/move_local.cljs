(ns amble-client.move-local
  "Handles moves made locally."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.environment :refer [environment]]
            [haslett.client :as haslett-client])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))

(def move-chan (async/chan))

(go-loop [app-atom (async/<! app-atom-chan)]
  (let [{:keys [player-id, player-piece-index, x, y] :as move} (async/<! move-chan)]
    (println "Handling local move.")
    (swap! app-atom assoc-in [:player-pieces player-id player-piece-index] [x, y])
    ;; (println move)
    (recur app-atom)))

(defmethod ig/init-key :amble/move-local [_ {:keys [app-atom, move-local-chan]}]
  (async/pipe move-local-chan amble-client.move-local/move-chan)
  (js/setTimeout (fn [& args]
                   (async/put! app-atom-chan app-atom))
                 1000) 
  nil)


