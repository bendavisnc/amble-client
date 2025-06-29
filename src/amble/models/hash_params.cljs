(ns amble.models.hash-params
  "Provides a coeffects injector interceptor for hash params found from the address bar url."
  (:require
   [goog.string :as gstring]
   [re-frame.core :as re-frame]))

(defn parse-hash-params [query]
  (let [hash (.substring query 1) ; remove the leading '#'
        params (js/URLSearchParams. hash)]
    (into {}
          (for [key (js->clj (.keys params))]
            [(keyword key)
             (.get params key)]))))

;; Adds a key for hash param values from the address bar
;; Based on whatever follows the `#` in the URL.
(def interceptor
  (re-frame/->interceptor
    :id ::interceptor
    :before (fn [context]
              (let [cofx (:coeffects context)]
                (assoc context :coeffects
                       (assoc cofx
                              :hash-params
                              (parse-hash-params (.-hash js/location))))))))

(re-frame/reg-event-fx
  ::update
  [interceptor]
  (fn [{:keys [db, hash-params]} [_]]
    (println (gstring/format "Applying hash params, `%s`."
                             hash-params))
    {:db
     (if-let [player-id-from-addressbar (some-> hash-params :player keyword)]
       (assoc-in db [:game :settings :player] player-id-from-addressbar)
       db)}))
