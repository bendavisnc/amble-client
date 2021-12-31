(ns amble-client.v3.global-state)

(defn get [s & ks]
  (println ks)
  (get-in s ks))
