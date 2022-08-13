(ns amble-client.interpolate-function
  "Provides an api for calling a provided function `interpolation-discreet-count` number of times.
   The function provided is always called with an x and y value derived from interpolating a given two coordinates."
  (:require [cljs.core.async :as async])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def time-duration 250)

;; How many times the animation function provided is called
(def interpolation-discreet-count 24)

(def f-chan (async/chan))
(def line-points-chan (async/chan))

(defn loop-animation [f, x1, y1, x2, y2]
  (let [wait-time-amount 
        (.floor js/Math
                (/ (* 1.0 time-duration)  
                   interpolation-discreet-count))]
        ;; _ (println wait-time-amount)]
    (letfn [
            (recursive-call [index]
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
                  (f {:x xi
                      :y yi})
                  (js/setTimeout (fn []
                                   (recursive-call (inc index)))
                                 wait-time-amount))))]
                                 
      (recursive-call 0))))

(defn interpolate-function [f & {:keys [x1, y1, x2, y2]}]
  (loop-animation f, x1, y1, x2, y2))
