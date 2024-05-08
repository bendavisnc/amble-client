(ns amble-client.moves-table
  (:require [integrant.core :as ig]
            ["react" :as react]
            ["react-router-dom" :as react-router-dom]
            ["ag-grid-react" :as ag-grid-react]))

(defn- moves-table [app-atom]
  (fn []
    [:f> (fn []
           (let [rows (:moves @app-atom)
                 columns [{:field "id"
                           :headerName "#"
                           :flex 1}
                          {:field "player"
                           :valueGetter (fn [e] 
                                          (let [e (js->clj (.-data e)
                                                           :keywordize-keys true)]
                                            (:player-id e)))
                           :flex 1}
                          {:field "piece"
                           :valueGetter (fn [e] 
                                          (let [e (js->clj (.-data e)
                                                           :keywordize-keys true)]
                                            (:player-piece-index e)))
                           :flex 1}]]
             [:div {:id "moves-table-container"}
               [:> react-router-dom/Form {:id "moves-table-form"
                                          :method "post"} 
                 [:div {:class "ag-theme-alpine"     
                        :id "grid-container"} 
                   [:> ag-grid-react/AgGridReact {:rowData rows 
                                                  :columnDefs columns
                                                  :pagination true
                                                  :paginationPageSize 5}]] 
                 [:button {:type "submit"}
                   "Delete"]]]))]))


(defmethod ig/init-key :amble/moves-table [_, {:keys [app-atom]}]
  (moves-table app-atom))
