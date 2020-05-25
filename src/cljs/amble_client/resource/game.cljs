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

(def interceptor-custom {
                         :name ::interceptor
                         :leave (fn [req]
                                  (.log js/console "hello hello")
                                  (.log js/console (clj->js req))
                                  (assoc-in req
                                            [:request, :with-credentials?]
                                            false))})

; (def interceptors-custom (-> martian-http/default-interceptors
                            ;  drop-last
                            ;  (concat [interceptor-custom])
                            ;  (concat [(last martian-http/default-interceptors)])))

(def interceptors-custom (-> martian-http/default-interceptors
                             (concat [interceptor-custom])))
                             
     
(def api-promise
  (new js/Promise (fn [resolve-callback, reject-callback]
                    (casync/go
                      (resolve-callback (casync/<! (martian-http/bootstrap-swagger url-openapi {:interceptors interceptors-custom}))))
                    nil)))

(defn response-promise [endpoint-key, param-map]
  (.then api-promise
    (fn [api]
      (.log js/console "this api...")
      (.log js/console api)
      (aset api "api_root" "http://localhost:3000")
      (new js/Promise (fn [resolve-callback, reject-callback]
                        (casync/go
                          (resolve-callback (casync/<! (martian/response-for api endpoint-key param-map))))
                        nil)))))


(defn get! [game-id]
  (response-promise :get-game-by-id {:game-id game-id}))
 
 
