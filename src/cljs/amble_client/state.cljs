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

(defn init! [& {:keys [game-id, designatee-coords, player-coords]}]
  (update! :game-id game-id)
  (update! :placement :designatee designatee-coords)
  (dorun
   (doseq [[player-id, coords] player-coords]
     (update! :placement
              :player
              (.indexOf [:player-one, :player-two, :player-three, :player-four, :player-five, :player-six]
                        player-id)
              coords))))

(defn print! []
  (.log js/console (clj->js (deref atomic-state))))
