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
           :port {; :client "3001"
                  :client "3449"
                  :amble  "3000"}}
        kv (((first keys) e)
            (second keys))]
    kv))

(defn url-ambel []
  (str "http://" (environment :host :amble) ":" (environment :port :amble)))

(defn url-openapi []
  (str "http://" (environment :host :client) ":" (environment :port :client) "/openapi.json"))

(def interceptor-coors-dont-bother-me {:name  ::interceptor-coors-dont-bother-me
                                       :leave (fn [req]
                                                (assoc-in req
                                                          [:request, :with-credentials?] ;; Don't be bothered by cors for now.
                                                          false))})

(defn interceptor-errors-thrown [c]
  {:name  ::interceptor-errors-handled
   :leave (fn [req]
            (let [response
                  (:response req)
                  error-text (first (filter (fn [x]
                                              (< 0 (count x)))
                                            [(:error-text response)]))]
              (when error-text
                (casync/put! c
                             (new js/Error (str "Error while making backend service api request.\n" error-text))))
              req))})

(defn api-chan* []
  (let [c (casync/chan)]
    (go
      (let [api
            (casync/<! (martian-http/bootstrap-swagger (url-openapi)
                                                       {:interceptors
                                                        (concat martian-http/default-interceptors
                                                                [interceptor-coors-dont-bother-me])}))]
        (loop []
          (casync/>! c api)
          (recur))))
    c))

(def api-chan (memoize api-chan*))

(defn response-chan [{:keys [endpoint-key, param-map, request-body]}]
  (go
    (let [api (casync/<! (api-chan))
          _ (aset api                                       ;; I'm unsure why this can't be done with assoc.
                  "api_root"
                  (url-ambel))
          _ (assert (martian/explore api endpoint-key)
                    (str "No api defined endpoint, \""
                         (name endpoint-key)
                         "\"."))
          all-params (if request-body (assoc param-map ::martian/request request-body)
                         param-map)
          error-chan (casync/chan)
          api-with-errors-thrown (update api :interceptors conj (interceptor-errors-thrown error-chan))
          response-chan (martian/response-for api-with-errors-thrown endpoint-key all-params)
          [response-or-error, _] (casync/alts! [error-chan, response-chan])]
      response-or-error)))
