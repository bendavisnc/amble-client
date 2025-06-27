(ns amble.views.error)

;; 🪿🔥

(defn error-rep [{:keys [error, message]}]
  (let [http-error? (-> message
                        (.includes "http-error"))
        reps (cond http-error?
                   (str "🚨"
                        "📡"
                        "🪿")
                   :else
                   (str "🚨"
                        "🪿"))]
    [:svg {:view-box "0 0 1 1"}
     [:text {:text-anchor "middle"
             :x "0.5"
             :y "0.5"
             :style {:font-size "0.25px"}}
            reps]]))

(defn component [error]
  [:div#amble
   [:div#error {:alt (error :message)}
    [error-rep error]]])
