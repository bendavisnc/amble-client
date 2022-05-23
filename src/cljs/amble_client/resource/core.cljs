(ns amble-client.resource.core
  (:require
   [clojure.string :as string]
   [cljs.js :refer [eval]]
   [amble-client.resource.environment :refer [environment]]
   [cljs.core.async :as casync]
   [martian.core :as martian]
   [martian.cljs-http :as martian-http])
  (:require-macros
   [cljs.core.async :refer [go go-loop]]))

(defn url-ambel []
  (str "http://" (environment :host :amble) ":" (environment :port :amble)))

(defn url-openapi []
  (str "http://" (environment :host :client) ":" (environment :port :client) "/openapi.json"))

(def interceptor-coors-dont-bother-me {:name  ::interceptor-coors-dont-bother-me
                                       :leave (fn [req]
                                                (-> req
                                                    (assoc-in [:request, :with-credentials?] ;; Don't be bothered by cors for now.
                                                              false)))})
                                                    ;; (assoc-in [:request, :headers :mode] ;; Don't be bothered by cors for now.
                                                              ;; "no-cors")))})






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

(defn response-chan [{:keys [endpoint-key, param-map]}]
  (go
    (let [api (casync/<! (api-chan))
          _ (aset api                                       ;; I'm unsure why this can't be done with assoc.
                  "api_root"
                  (url-ambel))
          _ (assert (martian/explore api endpoint-key)
                    (str "No api defined endpoint, \""
                         (name endpoint-key)
                         "\"."))
          error-chan (casync/chan)
          response-chan (martian/response-for api endpoint-key param-map)
          [response-or-error, _] (casync/alts! [error-chan, response-chan])
          body-maybe-lifted (or (:body response-or-error)
                                response-or-error)]
      body-maybe-lifted)))
