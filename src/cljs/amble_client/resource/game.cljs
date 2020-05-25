(ns amble-client.resource.game
  (:require
   [clojure.string :as string]
   [cljs.core.async :as casync]
   [martian.core :as martian]
   [martian.cljs-http :as martian-http]))

      
(def host "localhost")

(def port 3001)

(def url-openapi
  (str "http://" host ":" port "/openapi.json"))

(def api-promise
  (new js/Promise (fn [resolve-callback, reject-callback]
                    (casync/go
                      (resolve-callback (casync/<! (martian-http/bootstrap-swagger url-openapi))))
                    nil)))

(defn get! [game-id]
  (.then api-promise
    (fn [api]
      (martian/explore api))))
  
