(ns amble.models.hash-params
  "Provides a coeffects injector interceptor for hash params found from the address bar url."
  (:require
   [clojure.string :as string]
   [re-frame.core :as re-frame]))

(defn- parse-hash-params [hash]
  (let [cleaned (subs hash 1)
        pairs (string/split cleaned #"&")]
    (into {}
          (map #(let [[k v] (string/split % #"=")]
                  [(keyword k) v])
               pairs))))

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
    {:db
     (if-let [player-id-from-addressbar (some-> hash-params :player keyword)]
       (assoc-in db [:game :settings :player] player-id-from-addressbar)
       db)}))
