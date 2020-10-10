(ns amble-client.resource.core
  (:require
    [clojure.string :as string]
    [cljs.js :refer [eval]]
    [cljs.core.async :as casync]
    [martian.core :as martian]
    [martian.cljs-http :as martian-http]))

(defn environment [& keys]
  (let [e {:host {:client "localhost"
                  :amble  "localhost"}
           :port {
                  ; :client "3001"
                  :client "3449"
                  :amble  "3000"}}
        kv (((first keys) e)
            (second keys))]
    kv))


(defn url-ambel []
  (str "http://" (environment :host :amble) ":" (environment :port :amble)))

(defn url-openapi []
  (str "http://" (environment :host :client) ":" (environment :port :client) "/openapi.json"))


(def interceptor-custom {
                         :name ::interceptor
                         :leave (fn [req]
                                  (assoc-in req
                                            [:request, :with-credentials?] ;; Don't be bothered by cors for now.
                                            false))})


(def interceptors-custom (-> martian-http/default-interceptors
                             (concat [interceptor-custom])))

(defn load-openapi! []
  (martian-http/bootstrap-swagger (url-openapi)
                                  {:interceptors interceptors-custom}))


(def api-promise
  (new js/Promise (fn [resolve-callback, reject-callback]
                    (try
                      (casync/take! (load-openapi!)
                                    resolve-callback)
                      (catch :default err
                        (println "Problem with getting resource api based on openapi url.")
                        (reject-callback err))))))

(defn response-promise [{:keys [endpoint-key, param-map, request-body]}]
  (.then api-promise
    (fn [api]
      (let [all-params (if request-body (assoc param-map ::martian/request request-body)
                           param-map)]
        (aset api "api_root"
                  (url-ambel))
        (new js/Promise (fn [resolve-callback, reject-callback]
                          (try
                            (assert (martian/explore api endpoint-key)
                                    (str "No api defined endpoint, \""
                                          (name endpoint-key)
                                          "\"."))
                            (casync/take! (martian/response-for api endpoint-key all-params)
                                          resolve-callback)
                            (catch js/Error err
                              (reject-callback err)
                              (println "not wrong")
                              (println err)))))))))
