(ns amble-client.move-remote
  "Handles moves made remotely"
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.environment :refer [environment]]
            [amble-client.interpolate-function :refer [interpolate-function]]
            [haslett.client :as haslett-client])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))

(def move-chan (async/chan))

(go-loop [app-atom (async/<! app-atom-chan)]
  (let [{:keys [player-id, player-piece-index, x, y, move]} (async/<! move-chan)
        [last-x, last-y] (last move)]
    (println "Handling remote move.")
    (interpolate-function (fn [{:keys [x, y]}]
                            (swap! app-atom assoc-in [:player-pieces (keyword player-id) (js/parseInt player-piece-index)] [x, y]))
                          :x1 last-x
                          :y1 last-y
                          :x2 x
                          :y2 y)
                     
    ;; (println move)
    (recur app-atom)))


(defmethod ig/init-key :amble/move-remote [_ {:keys [app-atom, move-remote-chan]}]
  (async/pipe move-remote-chan amble-client.move-remote/move-chan)
  (js/setTimeout (fn [& args]
                   (async/put! app-atom-chan app-atom))
                 1000) 
  nil)


