(ns amble-client.move-replay
  "Provides action for replaying a move."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.environment :refer [environment]]
            [haslett.client :as haslett-client])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))

(defn replay-move [move]
  (println "at place move, come back to"))

(defmethod ig/init-key :amble/move-replay [_ {:keys [app-atom]}]
  (js/setTimeout (fn [& args]
                   (async/put! app-atom-chan app-atom))
                 1000) 
  {:replay-move replay-move})


