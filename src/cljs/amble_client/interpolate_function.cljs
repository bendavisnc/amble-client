(ns amble-client.interpolate-function
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

(go (loop []
      (let [f (async/<! f-chan)
            line-points (async/<! line-points-chan)
            [x1, y1, x2, y2] line-points]
        (loop-animation f, x1, y1, x2, y2))
        ;; (f {:x x2 :y y2}));}))
      (recur)))
        

(defn interpolate-function [f & {:keys [x1, y1, x2, y2]}]
  (async/put! f-chan f)
  (async/put! line-points-chan [x1, y1, x2, y2])
  nil)
  
