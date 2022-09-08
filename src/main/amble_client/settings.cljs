(ns amble-client.settings
  (:require [integrant.core :as ig]
            ["react-settings-pane" :refer [SettingsPane, SettingsMenu, SettingsContent, SettingsPage]]))

(def settings-config {"amble-settings.game.player" "player-one" 
                      "amble-settings.game.otherwuttt" "wutvalue"}) 

(def menu-config [{"title" "player", "url" "/wut/urlthing"} 
                  {"title" "wutotherthing", "url" "/wut/urlthingother"}])
                  
(def dynamic-stuff (clj->js [{:key "playerkey", 
                              :label "playerrr"
                              :type "text"} 
                             {:key "playerkey1", 
                              :label "playerrr2"
                              :type "text"}]))
(defn settings []
  (fn []
    [:> SettingsPane {:settings settings-config 
                      :items menu-config
                      :on-pane-leave #(println "neat")
                      :index "/wut/urlthing"} 
      [:> SettingsMenu {:headline "wutheadlinewutbut"}]                   
      [:> SettingsContent {:close-button-class "secondary"                   
                            :save-button-class "primary"
                            :header true} 
        ;;  [:> SettingsPage {:handler "/wut/urlthing"                   
        ;;                    :options dynamic-stuff}]
         [:> SettingsPage {:handler "/wut/urlthing"}                   
           [:fieldset {:class "form-group"}                   
             [:label {:for "wutt"}]                   
             [:input {:type "text"                   
                      :class" form-control"
                      :name "amble-settings.game.otherwuttt"}]]]]]))
    ;; <SettingsContent closeButtonClass= "secondary" saveButtonClass= "primary" header= {true} >
      ;;  <SettingsPage handler="/settings/general">
      ;;   <fieldset className="form-group">
      ;;   <label for="profileName">Name: </label>
      ;;   <input type="text" className="form-control" name="mysettings.general.name" placeholder="Name" id="general.ame" onChange={settingsChanged} defaultValue={settings['mysettings.general.name']} />
      ;;   </fieldset>]]))

(defmethod ig/init-key :amble/settings [_ {:keys []}]
  (settings))


