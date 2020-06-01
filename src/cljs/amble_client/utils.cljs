(ns amble-client.utils
  (:require [amble-client.resource.game :as game-resource]
            [clojure.string :as string]))

(defn game-id-from-window
  "Returns the game id from the browser window."
  []
  (let [game-id
        (-> js/window
            (aget "location")
            (aget "pathname")
            (string/split "/")
            last)]
    (when (not game-id)
      (throw (new js/Error "No game id found in browser url.")))
    game-id))

(defn init-secret-post-game! []
  (let [post-fn
        (fn []
          (.then (game-resource/create!)
                 (fn [response-result]
                   (.log js/console "Requested new game.")
                   (.log js/console response-result))))]
    (aset js/window "amble" (clj->js {:game-create
                                      post-fn}))))

