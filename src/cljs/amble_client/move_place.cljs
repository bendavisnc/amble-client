(ns amble-client.move-place
  "Provides action for placing piece after the end of a move."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.environment :refer [environment]]
            [haslett.client :as haslett-client])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))

(defn place-move [move]
  (println "at place move, come back to"))


(defmethod ig/init-key :amble/move-place [_ {:keys [app-atom]}]
  (js/setTimeout (fn [& args]
                   (async/put! app-atom-chan app-atom))
                 1000) 
  {:place-move place-move}
  nil)


