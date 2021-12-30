(ns amble-client.v3.global-state)

(defn get [s & ks]
  (get-in s ks))
