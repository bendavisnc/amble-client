(ns amble-client.interpolate-function
  (:require [cljs.core.async :as async])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

;; Animations last a second, by default.
(def time-duration 1000)

;; How many times the animation function provided is called
(def interpolation-discreet-count 60)

(def f-chan (async/chan))
(def line-points-chan (async/chan))

(defn loop-animation [f, x1, y1, x2, y2]
  (loop [index 0]
    (if (< interpolation-discreet-count 
           index)
      (println (str "Finished animation, time," (new js/Date) "."))
      (let [i (/ index 
                 (* 1.0 
                    interpolation-discreet-count))
            xi (+ x1
                  (* i
                     (- x2 x1)))
            yi (+ y1
                  (* i
                     (- y2 y1)))]
        (println [i, xi, yi])
        (f xi yi)
        (recur (inc index))))))

(go (loop []
      (let [f (async/<! f-chan)
            line-points (async/<! line-points-chan)
            [x1, y1, x2, y2] line-points]
        (loop-animation f, x1, y1, x2, y2))
      (recur)))
        

(defn interpolate-function [f & {:keys [x1, y1, x2, y2]}]
  (async/put! f-chan f)
  (async/put! line-points-chan [x1, y1, x2, y2])
  nil)
  
