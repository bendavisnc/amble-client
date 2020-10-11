(ns amble-client.resource.core
  (:require
    [clojure.string :as string]
    [cljs.js :refer [eval]]
    [cljs.core.async :as casync]
    [martian.core :as martian]
    [martian.cljs-http :as martian-http])
  (:require-macros
    [cljs.core.async :refer [go go-loop]]))

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
                         :name  ::interceptor
                         :leave (fn [req]
                                  (assoc-in req
                                            [:request, :with-credentials?] ;; Don't be bothered by cors for now.
                                            false))})


(def interceptors-custom (-> martian-http/default-interceptors
                             (concat [interceptor-custom])))

(defn api-chan []
  (martian-http/bootstrap-swagger (url-openapi)
                                  {:interceptors interceptors-custom}))

(defn response-chan [{:keys [endpoint-key, param-map, request-body]}]
  (let [c (casync/chan)]
    (go
      (let [api (casync/<! (api-chan))
            _ (println "what")
            _ (println endpoint-key)
            _ (println param-map)
            all-params (if request-body (assoc param-map ::martian/request request-body)
                                        param-map)
            _ (println all-params)
            _ (aset api "api_root" (url-ambel))
            _ (assert (martian/explore api endpoint-key)
                      (str "No api defined endpoint, \""
                           (name endpoint-key)
                           "\"."))
            response (casync/<! (martian/response-for api endpoint-key all-params))]
        (println "whatck")
        (println response)
        (casync/>! c response)))
    c))
