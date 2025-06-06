(ns amble.models.models)

(defn db-to-game-id  [db]
  (let [game-id (get-in db [:game :game-id])
        _ (when (not game-id)
            (throw (new js/Error "No game id found.")))]
    game-id))
