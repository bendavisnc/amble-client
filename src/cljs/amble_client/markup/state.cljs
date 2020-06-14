(ns amble-client.markup.state
  (:require [reagent.core :as reagent]))

(def atomic-state (reagent/atom {:errors []}))

(defn update! [& args]
  (cond (= 2 (count args))
        (let [[k, v] args]
          (swap! atomic-state assoc k v))
        :else
        (throw (new js/Error (str "Don't know how to update app state with args, \""
                                  args
                                  "\".")))))

(defn get [k]
  (k (deref atomic-state)))

(defn print! []
  (.log js/console (clj->js (deref atomic-state))))

