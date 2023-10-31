(ns amble-client.moves-table
  (:require [integrant.core :as ig]
            ["react" :as react]
            ["ag-grid-react" :as ag-grid-react]))

(defn- moves-table [app-atom]
  (fn []
    [:f> (fn []
           (let [rows (:moves @app-atom)
                 columns [{:field "id"
                           :headerName "#"},
                          {:field "player"
                           :valueGetter (fn [e] 
                                          (let [e (js->clj (.-data e)
                                                           :keywordize-keys true)]
                                            (:player-id e)))}
                          {:field "piece"
                           :valueGetter (fn [e] 
                                          (let [e (js->clj (.-data e)
                                                           :keywordize-keys true)]
                                            (:player-piece-index e)))}]]
             [:div {:class "ag-theme-alpine"     
                    :id "grid-container"} 
               [:> ag-grid-react/AgGridReact {:rowData rows 
                                              :columnDefs columns}]]))]))

(defmethod ig/init-key :amble/moves-table [_, {:keys [app-atom]}]
  (moves-table app-atom))
