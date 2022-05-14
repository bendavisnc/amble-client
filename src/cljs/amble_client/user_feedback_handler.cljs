(ns amble-client.user-feedback-handler
  (:require [integrant.core :as ig]))

(defn handle-ui-event [e]
  (println "neat feedback todo")
  (println e))
