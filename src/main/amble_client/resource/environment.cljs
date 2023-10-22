(ns amble-client.resource.environment)
(def ip "192.168.50.72")

(defn environment [& keys]
  (let [e {:host {;;:client "0.0.0.0"
                  :client ip
                  ;; :amble  "0.0.0.0"}
                  :amble ip}
           :port {; :client "3001"
                  :client "8080"
                  :amble "3000"}}
        kv (((first keys) e)
            (second keys))]
    kv))
