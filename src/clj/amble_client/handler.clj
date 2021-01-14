(ns amble-client.handler
  (:require
   [clojure.java.io :as io]
   [reitit.ring :as reitit-ring]
   [amble-client.middleware :refer [middleware]]
   [hiccup.page :refer [include-js include-css html5]]
   [config.core :refer [env]]))

(def mount-target
  [:div#app
   [:h2 "Welcome to amble-client"]
   [:p "please wait while Figwheel is waking up ..."]
   [:p "(Check the js console for hints if nothing exciting happens.)"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css
    (if (env :dev) "/css/site.css" "/css/site.min.css"))
   (include-css "/css/wesandersontry.css")])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "/js/app.js")]))

(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(defn game-token []
  "heyImmaGameToken")

(defn redirect-handler
  [_request]
  {:status 301
   :headers {"Location"
             (str "/game/"
                  (game-token))}})

(defn openapi-handler
  [_request]
  (println "Serving openapi.")
  {:status 200
   :body (let [openapi (io/resource "public/json/openapi.json")]
           (when (nil? openapi)
             (throw (new Exception "No openapi made available.")))
           (slurp openapi))})

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get {:handler redirect-handler}}],
     ["/openapi.json" {:get {:handler openapi-handler}}],
     ["/game"
      ["/:game-id" {:get {:handler index-handler
                          :parameters {:path {:game-id int?}}}}]]])

   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware middleware}))
