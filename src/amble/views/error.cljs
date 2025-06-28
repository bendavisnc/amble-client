(ns amble.views.error)

;; 🪿🔥

(defn error-rep [{:keys [error, message]}]
  (let [http-error? (some-> message
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

(defn component [{:keys [message error]}]
  (let [caption (or (and (seq message)
                         message)
                    error)]
    [:div#amble
     [:div#error {:title caption}
      [error-rep error]]]))

(comment (seq ""))
