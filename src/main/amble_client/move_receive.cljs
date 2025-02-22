(ns amble-client.move-receive
  "Listens to moves from server. 
   Adds moves to `move-remote-chan`."
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [amble-client.move-helpers :refer [end-move-at-point!]]
            [goog.string :as gstring]
            [goog.string.format])
  (:require-macros [cljs.core.async :refer [go-loop]]))

(def move-index-chan (async/chan))
(def move-resource-get!-chan (async/chan))
(def app-atom-chan (async/chan))
(def move-chan (async/chan))
(def move-record-check-chan (async/chan))

(defn valid? [move]
  (when (seq (:move move))
    (assoc move :origin :remote)))

(defn existing? [app-atom, i]
  (first (filter (fn [m]
                   (= i (:id m)))
                 (:moves (deref app-atom)))))

(defn remove-move! [app-atom, move]
  (println (gstring/format "Removing move, `%s`"
                           (:id move)))
  (swap! app-atom 
         update 
         :moves 
         (fn [acc f]
           (filter f acc))
         (fn [m]
           (not (= (:id move)
                   (:id m)))))
  (println move)
  ;;(println (type (:player-id move))))
  (let [[x, y] (first (:move move))]
    (end-move-at-point! app-atom (-> move 
                                     (update :player-id keyword)
                                     (assoc :x x)
                                     (assoc :y y)
                                     (update :moves reverse)))))

(go-loop [move-resource-get! (async/<! move-resource-get!-chan)
          move-record-check (async/<! move-record-check-chan)
          app-atom (async/<! app-atom-chan)
          game-id (:game-id (deref app-atom))]
  (let [move-id (async/<! move-index-chan)
        _ (println (str "New move announced from server, id " move-id "."))
        move-from-server (async/<! (move-resource-get! game-id, (str move-id)))]
    (println "Requested move.")
    (if-let [move-valid (valid? move-from-server)]
      (do (println (gstring/format "Received server acknowledged move, `%s`"
                                   (:id move-valid)))
        (async/>! move-chan (assoc move-valid 
                                   :is-remote?  
                                   (not (move-record-check (:client-id move-valid))))))
      (if-let [move-existing (existing? app-atom, move-id)]
        (remove-move! app-atom move-existing)
        (throw (new js/Error (gstring/format "Move request failed `%s`."
                                             move-id)))))
    (recur move-resource-get!, move-record-check, app-atom, game-id)))

(defmethod ig/init-key :amble/move-receive [_ {:keys [app-atom, move-remote-chan, move-resource-get!, move-index-chan, move-record-check]}]
  (async/put! move-record-check-chan move-record-check)
  (async/pipe move-index-chan
              amble-client.move-receive/move-index-chan)
  (async/pipe amble-client.move-receive/move-chan
              move-remote-chan)
  (js/setTimeout (fn [& _]
                   (async/put! app-atom-chan app-atom))
                 1000)

  (async/put! move-resource-get!-chan move-resource-get!)
  nil)


