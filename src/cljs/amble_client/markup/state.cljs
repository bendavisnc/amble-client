(ns amble-client.markup.state
  (:require [reagent.core :as reagent]))

(def atomic-state (reagent/atom {:errors []}))

(defn update! [& args]
  (let [update-val (last args)
        assoc-keys (filter #(not (= update-val %))
                           args)]
    (cond (empty? assoc-keys)
          (throw (new js/Error (str "Invalid arguments provided, " args ".")))
          :else
          (swap! atomic-state assoc-in assoc-keys update-val))))

(defn get [& keys]
  (get-in (deref atomic-state)
          keys))

(defn print! []
  (.log js/console (clj->js (deref atomic-state))))

