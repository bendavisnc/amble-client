(ns amble-client.config 
  "Integrant config that expresses the app component dependency relationships."
  (:require 
            [integrant.core :as ig]))

(def config-map 
  {:amble/board {:board-pieces (ig/ref :amble/board-pieces)
                 :player-pieces 7}
   :amble/board-pieces 8})

