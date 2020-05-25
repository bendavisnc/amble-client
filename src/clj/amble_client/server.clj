(ns amble-client.server
    (:require
     [amble-client.handler :refer [app]]
     [config.core :refer [env]]
     [ring.adapter.jetty :refer [run-jetty]])
    (:gen-class))

(defn -main [& args]
  (let [port (or (env :port) 3000)]
    (println (str "Running amble server for client on port " port " ."))
    (run-jetty #'app {:port port :join? false})))
