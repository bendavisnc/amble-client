(ns amble-client.v3.global-state
  (:refer-clojure :exclude [get]))

(defn get [s & ks]
  (get-in s ks))

(defn set! [a, ks, v]
  (swap! a (fn [aa]
             (assoc-in aa ks v))))
                

