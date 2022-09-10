(ns amble-client.settings
  (:require [integrant.core :as ig]
            ["react-settings-pane" :refer [SettingsPane, SettingsMenu, SettingsContent, SettingsPage]]))

(def settings-config {"amble-settings.game.player" "player-one"}) 

(def menu-config [{"title" "game", "url" "#game"} 
                  {"title" "about", "url" "#about"}]) 
                  
(defn on-settings-change! [& args]
  (println "noiceee")
  (.log js/console args)
  (println (str (first args))))

(defn settings []
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
                       :default-value "player one"
                       :on-change on-settings-change!
                       :name "amble-settings.game.player"}
               [:option {:value "player-one", :selected "selected"} "player one"]
               [:option {:value "player-two"} "player two"]
               [:option {:value "player-three"} "player three"]
               [:option {:value "player-four"} "player four"]
               [:option {:value "player-five"} "player five"]
               [:option {:value "player-six"} "player six"]]]]
         [:> SettingsPage {:handler "#about"}                   
           [:div "\"amble\" is a web app to share a chinese checkers board amongst friends."]]]]))
    ;; <SettingsContent closeButtonClass= "secondary" saveButtonClass= "primary" header= {true} >
      ;;  <SettingsPage handler="/settings/general">
      ;;   <fieldset className="form-group">
      ;;   <label for="profileName">Name: </label>
      ;;   <input type="text" className="form-control" name="mysettings.general.name" placeholder="Name" id="general.ame" onChange={settingsChanged} defaultValue={settings['mysettings.general.name']} />
      ;;   </fieldset>]]))

            ;;  <select name="mysettings.general.color-theme" id="profileColor" className="form-control" defaultValue={settings['mysettings.general.color-theme']}>
            ;;    <option value="blue">Blue</option>
            ;;    <option value="red">Red</option>
            ;;    <option value="purple">Purple</option>
            ;;    <option value="orange">Orange</option>
            ;;  </select>]]]]))

(defmethod ig/init-key :amble/settings [_ {:keys []}]
  (settings))


