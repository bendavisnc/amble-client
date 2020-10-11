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
  (casync/pipeline-async
    1
    (casync/chan)
    (fn [api response-chan]
      (let [
            all-params (if request-body (assoc param-map ::martian/request request-body))
            _ (assert (martian/explore api endpoint-key)
                      (str "No api defined endpoint, \""
                           (name endpoint-key)
                           "\"."))]
        (casync/pipe
          (martian/response-for api endpoint-key all-params)
          response-chan)))
    (api-chan)))
