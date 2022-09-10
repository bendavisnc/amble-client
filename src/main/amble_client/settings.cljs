(ns amble-client.settings
  (:require [integrant.core :as ig]
            [cljs.core.async :as async]
            [goog.string :as gstring]
            [goog.string.format]
            ["react-settings-pane" :refer [SettingsPane, SettingsMenu, SettingsContent, SettingsPage]])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))

(def app-atom-chan (async/chan))

(def settings-config {"amble-settings.game.player" "player-one"}) 

(def menu-config [{"title" "game", "url" "#game"} 
                  {"title" "about", "url" "#about"}]) 
                  
(defn on-player-settings-change! [app-atom, player-selected, _]
  (println (gstring/format "New player setting selected, \"%s\"."
                           (name player-selected))) 
  (println (deref app-atom))
  (swap! app-atom assoc-in [:settings :player] player-selected))
  

(defn option [k, on-click]
  [:option {:value (name k)}
            ;; :on-click on-click} 
           (name k)])

(defn settings [app-atom]
  (fn []
   [:> SettingsPane {:settings settings-config 
                     :items menu-config
                     :on-pane-leave #(println "neat")
                     :index "#game"} 
      [:> SettingsMenu {:headline "⚙ General Settings"}]                   
      [:> SettingsContent {:close-button-class "secondary"                   
                           :save-button-class "primary"
                           :header true} 
         [:> SettingsPage {:handler "#game"}                   
           [:fieldset {:class "form-group"}                   
             [:label {:for "player-select"} "Player :"]                   
             [:select {
                       :class "form-control"
                       :id "player-select"
                       ;; :value (name player-selected)
                       :value (name (or (get-in (deref app-atom) 
                                               [:settings :player])
                                        :player-one))
                       :on-change (fn [e]
                                    (on-player-settings-change! app-atom 
                                                                (keyword (.-value (.-target e)))
                                                                e))
                       :name "amble-settings.game.player"}

               (for [player-key [:player-one, :player-two, :player-three, :player-four, :player-five, :player-six]]

                 ^{:key (str (name player-key) "-option")}
                 [option player-key (partial on-player-settings-change! app-atom player-key)])]]]
         [:> SettingsPage {:handler "#about"}                   
           [:div "\"amble\" iz a web app to share a chinese checkers board amongst friends."]]]]))

(defmethod ig/init-key :amble/settings [_ {:keys [app-atom]}]
  (settings app-atom))


