(ns amble-client.move-replay
  "Replays remote moves."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.resource.environment :refer [environment]]
            [haslett.client :as haslett-client])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

;; How long to wait between position updates
(def discreet-wait-time 24)
;; (def discreet-wait-time 100)

(def f-chan (async/chan))
(def move-chan (async/chan))
(def on-end-chan (async/chan))

(defn loop-animation [f, moves, on-end]
  (letfn [
          (recursive-call [index]
            (if (>= index
                    (count moves)) 
              (do
                (println (str "Finished replay, time," (new js/Date) "."))
                (when (on-end)
                  (on-end)))
              (let [move-next (nth moves index)
                    [x, y] move-next] 
                (assert (number? x) "point values aren't numbers")
                (f {:x x
                    :y y})
                (js/setTimeout (fn []
                                 (recursive-call (inc index)))
                               discreet-wait-time))))]
                               
    (recursive-call 0)))

(go (loop []
      (let [f (async/<! f-chan)
            move (async/<! move-chan)
            on-end (async/<! on-end-chan)]
        (loop-animation f 
                        (:move move)
                        on-end))
      (recur)))
        

(defn replay-move! [f, move, on-end]
  (async/put! f-chan f)
  (async/put! move-chan move)
  (async/put! on-end-chan on-end)
  nil)
 
