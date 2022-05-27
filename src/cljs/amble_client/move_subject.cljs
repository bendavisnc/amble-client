(ns amble-client.move-subject
  "Manages move observers."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.utils :as utils])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def observers-atom (atom []))

(defn add-observer! [observer]
  (swap! observers-atom conj observer))

(defn notify-on-move! [game-id, move-index, move]
  (do-run (for [observer @observers-atom]
            (if-let [f (:on-move! observer)]
              (f game-id, move-index, move)))))
             
(defn notify-on-move-xy! [game-id, move-index, x, y]
  (do-run (for [observer @observers-atom]
            (if-let [f (:on-move-xy! observer)]
              (f game-id, move-index, x, y)))))

(defmethod ig/init-key :amble/move-subject [_ {:keys []}]
  {:add-observer! add-observer! 
   :notify-on-move! notify-on-move! 
   :notify-on-move-xy! notify-on-move-xy!}) 


