(ns amble.views.error
  (:require
   [amble.errors :as errors]
   [re-frame.core :as re-frame]))

;; 🪿

(defn error-rep [{:keys [error, message]}]
  (let [http-error? (some-> message
                            (.includes "http-error"))
        reps (cond http-error?
                   "🚨📡🪿"
                   :else
                   "🗑🔥🪿")]
    [:svg {:view-box "0 0 1 1"}
     [:text {:text-anchor "middle"
             :x "0.5"
             :y "0.5"
             :style {:font-size "0.25px"}}
      reps]]))

(defn component [{:keys [message error]}]
  (let [caption (or (and (seq message)
                         message)
                    error)]
    [:div#amble
     [:div#error {:title caption}
      [error-rep error]]]))

(defn boundary [fallback]
  (let [errors (re-frame/subscribe [::errors/errors])]
    (if-let [error (some-> errors deref first)]
      [component error]
      fallback)))

(comment (seq ""))
