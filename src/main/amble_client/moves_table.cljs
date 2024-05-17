(ns amble-client.moves-table
  (:require [integrant.core :as ig]
            ["react" :as react]
            ["react-router-dom" :as react-router-dom]
            [reagent.core :as reagent]
            [cljs.core.async :as async]
            ["ag-grid-react" :as ag-grid-react]))

(def move-resource-delete!-atom (atom nil))

(defn delete-move! [app-atom, game-id, id]
  (let [move-resource-delete! (deref move-resource-delete!-atom)]
    (async/take! (move-resource-delete! game-id, id)
                 (fn [move-delete-response]
                   (println "Move deleted.")))))


(defn move-action-cell [app-atom]
  (fn [props]
    (reagent/as-element [:f>
                         (fn []
                           [:button {:on-click (fn [_] 
                                                 (let [[game-id, id]
                                                       (.-value props)]
                                                   (delete-move! app-atom, game-id, id)))}
                                    "delete!"])])))

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
                           :flex 1}
                          {:field "piece-action"
                           :valueGetter (fn [e] 
                                          (let [e (js->clj (.-data e)
                                                           :keywordize-keys true)]
                                            [(:game-id e), (:id e)]))
                           :cellRenderer (move-action-cell app-atom)
                           :flex 1}]]
             [:div {:id "moves-table-container"}
               [:> react-router-dom/Form {:id "moves-table-form"
                                          :method "post" 
                                          :on-submit (fn [e]
                                                       (.preventDefault e))
                                          :action "destroy"}
                 [:div {:class "ag-theme-alpine"     
                        :id "grid-container"} 
                   [:> ag-grid-react/AgGridReact {:rowData rows 
                                                  :columnDefs columns
                                                  :pagination true
                                                  :paginationPageSize 5}]]]]))])) 

(defmethod ig/init-key :amble/moves-table [_, {:keys [app-atom, move-resource-delete!]}]
  (reset! move-resource-delete!-atom move-resource-delete!)
  (moves-table app-atom))
