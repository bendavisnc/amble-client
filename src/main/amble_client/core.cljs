(ns amble-client.core
  (:require [cljs.core.async :as async]
            [integrant.core :as ig]
            ["react" :as react]
            ["react-burger-menu" :as react-burger-menu]
            [reagent.core :as reagent]
            [reagent.dom :as reagent-dom]
            [reagent.ratom :as reagent-ratom]
            ["react-router-dom" :as react-router-dom]
            [amble-client.board-pieces]
            [amble-client.board]
            [amble-client.game-id]
            [amble-client.moves-table]
            [amble-client.settings]
            [amble-client.board-piece-closest]
            [amble-client.board-piece-active]
            [amble-client.app]
            [amble-client.moves]
            [amble-client.player-pieces]
            [amble-client.resource.board :as board-resource]
            [amble-client.resource.game :as game-resource]
            [amble-client.resource.player :as player-resource]
            [amble-client.resource.move :as move-resource]
            [amble-client.utils :as utils]
            [amble-client.game-play]
            [amble-client.move-send]
            [amble-client.move-async]
            [amble-client.move-local]
            [amble-client.move-remote]
            [amble-client.move-receive]
            [amble-client.move-record-check])
  (:require-macros [cljs.core.async :refer [go]]))

;; (def game-id (utils/game-id-from-window))
;; (assert game-id "Problem getting game-id from browser url.")
(def app-atom (reagent-ratom/atom {}))
(def app-ready-chan (async/chan))
(def app-ready-chan-multicast (async/mult app-ready-chan))
(def move-local-chan (async/chan))
(def move-remote-chan (async/chan))
(def move-local-chan-multicast (async/mult move-local-chan))
(def move-remote-chan-multicast (async/mult move-remote-chan))
(def move-xy-chan (async/chan))
(def move-xy-chan-multicast (async/mult move-xy-chan))
(def latest-move-index-chan (async/chan))

(defn move-local-chan-dup []
  (let [c (async/chan)]
    (async/tap move-local-chan-multicast c)
    c))

(defn move-remote-chan-dup []
  (let [c (async/chan)]
    (async/tap move-remote-chan-multicast c)
    c))

(defn move-xy-chan-dup []
  (let [c (async/chan)]
    (async/tap move-xy-chan-multicast c)
    c))

(defn app-ready-chan-dup []
  (let [c (async/chan)]
    (async/tap app-ready-chan-multicast c)
    c))

(def app-config {:amble/app {:app-atom app-atom
                             :board (ig/ref :amble/board)
                             :game-id (ig/ref :amble/game-id)}

                 :amble/board {:app-atom app-atom
                               :board-pieces (ig/ref :amble/board-pieces)
                               :player-pieces (ig/ref :amble/player-pieces)
                               :game-play (ig/ref :amble/game-play)}


                 :amble/game-id {:app-atom app-atom 
                                 :app-ready-chan app-ready-chan}
                 :amble/moves-table {:app-atom app-atom}
                 :amble/settings {:app-atom app-atom}
                 :amble/board-pieces {:app-atom app-atom
                                      :app-ready-chan (app-ready-chan-dup)}
                 :amble/player-pieces {:app-atom app-atom
                                       :game-play (ig/ref :amble/game-play)
                                       :move-xy-chan (move-xy-chan-dup)
                                       :app-ready-chan (app-ready-chan-dup)}
                 :amble/game-play {:move-local-chan move-local-chan
                                   :move-xy-chan move-xy-chan
                                   :board-piece-closest (ig/ref :amble/board-piece-closest)
                                   :app-atom app-atom}
                 :amble/moves {:app-atom app-atom
                               :move-remote-chan (move-remote-chan-dup) 
                               :game-id (ig/ref :amble/game-id)} 
                 :amble/move-send {:move-local-chan (move-local-chan-dup)
                                   :move-resource-add! move-resource/add!}
                 :amble/move-receive {:move-resource-get! move-resource/get!
                                      :latest-move-index-chan latest-move-index-chan
                                      :move-remote-chan move-remote-chan
                                      :app-atom app-atom}
                 :amble/move-async {:latest-move-index-chan latest-move-index-chan
                                    :app-atom app-atom
                                    :app-ready-chan (app-ready-chan-dup)}
                 :amble/move-local {:app-atom app-atom
                                    :move-local-chan (move-local-chan-dup)
                                    :app-ready-chan (app-ready-chan-dup)}
                 :amble/move-remote {:app-atom app-atom
                                     :app-ready-chan (app-ready-chan-dup)
                                     :move-remote-chan (move-remote-chan-dup)
                                     :move-record-check (ig/ref :amble/move-record-check)}
                 :amble/move-record-check {:move-local-chan (move-local-chan-dup)}

                 :amble/board-piece-closest {:app-atom app-atom
                                             :app-ready-chan (app-ready-chan-dup)}
                 :amble/board-piece-active {:app-atom app-atom
                                            :move-xy-chan (move-xy-chan-dup)
                                            :board-piece-closest (ig/ref :amble/board-piece-closest)
                                            :app-ready-chan (app-ready-chan-dup)}})

;;  :amble/app-atom app-atom
                ;;  :amble/user-feedback-handler user-feedback-handler/handle-ui-event

(defn AppElement []
  (fn [config]
    (let [config (js->clj config :keywordize-keys true)]
      (reagent/as-element [(:app config)])))) 

(defn MovesElement []
  (fn [config]
    (let [config (js->clj config :keywordize-keys true)]
      (println config)
      (reagent/as-element [(:movesTable config)])))) 

(defn SettingsElement []
  (fn [config]
    (let [config (js->clj config :keywordize-keys true)]
      (reagent/as-element [(:settings config)])))) 

(defn hamburger-menu [links]
  [:> react-burger-menu/push {:outerContainerId "root" 
                              :pageWrapId "content-outlet"}
                             links]) 
                                    


(defn nav-link [name location]
  [:> react-router-dom/NavLink {:to location
                                :key location
                                :end true
                                :class (fn [params]
                                         (let [{:keys [isActive, isPending]}
                                               (js->clj params :keywordize-keys true)]
                                           (if isActive
                                             "active"
                                             (if isPending
                                               "pending"
                                               ""))))}
                               name])
 
 

(defn RootElement []
  [:f> (fn [] 
         (let [game-id (.-gameId (react-router-dom/useParams))]
           [:div {:id "root"}
             [hamburger-menu [:<> [nav-link "board" (str "/game/" game-id)] 
                                  [nav-link "moves" (str "/game/" game-id "/moves")]
                                  [nav-link "settings" (str "/game/" game-id "/settings")]]]
                             
             [:div {:id "content-outlet"}
               [:> react-router-dom/Outlet]]]))])

(defn router [config]
  (react/createElement react-router-dom/RouterProvider 
                       (clj->js {:router (react-router-dom/createBrowserRouter (clj->js [{:path "/"
                                                                                          :element (reagent/as-element [RootElement])
                                                                                          :children [{:path "/game/:gameId"
                                                                                                      :element (reagent/as-element [:> (AppElement) config])}
                                                                                                     {:path "/game/:gameId/moves"
                                                                                                      :element (reagent/as-element [:> (MovesElement) config])}
                                                                                                     {:path "/game/:gameId/settings"
                                                                                                      :element (reagent/as-element [:> (SettingsElement) config])}]}]))})))
  
(defn mount-root []
  (let [_ (println "Initializing.")
        app-config-initialized (ig/init app-config)
        _ (.log js/console app-config-initialized)]
    (reagent-dom/render (router app-config-initialized)
                        (.getElementById js/document "app")
                        (fn []
                          (println "App is mounted.")))))

(defn init! []
  (swap! app-atom assoc-in [:settings :player] :player-one)
  (mount-root))

(defn post-game! []
  (async/take! (game-resource/create!)
               (fn [game-create-response]
                 (println "Requested new game.")
                 (println game-create-response))))
