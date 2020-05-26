(ns amble-client.resource.core
  (:require
   [clojure.string :as string]
   [cljs.core.async :as casync]
   [martian.core :as martian]
   [martian.cljs-http :as martian-http]))

(def ^:dynamic *env* nil)

(defn environment [& keys]
  (.log js/console "idk man")
  (when (not *env*)
    (throw (new js/Error "No env.")))
  (.log js/console *env*)
  (apply *env* keys))
      
(defn url-openapi []
  (str "http://" (environment :host :client) ":" (environment :port :client) "/openapi.json"))

(def interceptor-custom {
                         :name ::interceptor
                         :leave (fn [req]
                                  (.log js/console "hello hello")
                                  (.log js/console (clj->js req))
                                  (assoc-in req
                                            [:request, :with-credentials?] ;; Don't be bothered by cors for now.
                                            false))})


(def interceptors-custom (-> martian-http/default-interceptors
                             (concat [interceptor-custom])))
                             
     
(def api-promise
  (new js/Promise (fn [resolve-callback, reject-callback]
                    (casync/go
                      (resolve-callback (casync/<! (martian-http/bootstrap-swagger (url-openapi) 
                                                                                   {:interceptors interceptors-custom}))))
                    nil)))

(defn response-promise [endpoint-key, param-map]
  (.then api-promise
    (fn [api]
      (.log js/console "this api...")
      (.log js/console api)
      (aset api "api_root" 
                (str "http://" (environment :host :amble) ":" (environment .:port :amble) "/"))
      (new js/Promise (fn [resolve-callback, reject-callback]
                        (casync/go
                          (resolve-callback (casync/<! (martian/response-for api endpoint-key param-map))))
                        nil)))))
