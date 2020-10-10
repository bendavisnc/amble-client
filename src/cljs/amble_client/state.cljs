(ns amble-client.state
  "An api for all state getting and setting.
   Encapsulates reagent's atom to affect ui markup.
   Pretty much anything in this code base that has an exclamation point uses this dep."
  (:refer-clojure :exclude [get])
  (:require [reagent.core :as reagent]
            [amble-client.utils :as utils]))

(def atomic-state (reagent/atom {:errors []}))

(defn get [& keys]
  (get-in (deref atomic-state)
          keys))

(defn update! [& args]
  (let [update-val (last args)
        assoc-keys (filter #(not (= update-val %))
                           args)]
    (cond (empty? assoc-keys)
          (throw (new js/Error (str "Invalid arguments provided, " args ".")))
          :else
          (swap! atomic-state assoc-in assoc-keys update-val))))

(defn init-promise! [& {:keys [game-id, designatee-coords, piece-indexes]}]
  (new js/Promise (fn [resolve, reject]
                    (update! :game-id game-id)
                    (update! :placement :designatee designatee-coords)
                    (dorun
                      (doseq [i (range (count piece-indexes))]
                        (println (str "making piece "
                                      i))
                        (let [player-index i
                              indexes (nth piece-indexes i)]
                          (update! :placement
                                  :player
                                  player-index
                                  (vec
                                    (map vec
                                        (utils/pieces-inferred-by-index :designatee-coords designatee-coords
                                                                        :piece-indexes indexes)))))))
                    (println "dunzo (state)")
                    (resolve nil))))

(defn print! []
  (.log js/console (clj->js (deref atomic-state))))
