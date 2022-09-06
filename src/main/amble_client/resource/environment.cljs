(ns amble-client.resource.environment)

(defn environment [& keys]
  (let [e {:host {;;     :client "0.0.0.0"
                  :client "192.168.1.193"
              ;;     :amble  "0.0.0.0"
                  :amble "192.168.1.193"}
           :port {; :client "3001"
                  :client "3449"
                  :amble "3000"}}
        kv (((first keys) e)
            (second keys))]
    kv))
