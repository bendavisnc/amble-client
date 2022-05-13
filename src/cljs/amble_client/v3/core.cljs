;; (ns amble-client.v3.core
;;   (:require [cljs.core.async :as async]
;;             [goog.string :as gstring]
;;             [goog.string.format]
;;             [reagent.dom]
;;             [reagent.core :as reagent])
;;   (:require-macros [cljs.core.async :refer [go, go-loop]]))

;; (def reagent-global-state-atom )

;; (events/subscribe-after-init! (fn [{:keys [game-id]}]
;;                                 (board-service/get-landings (fn [landings]
;;                                                               (gs/set! reagent-global-state-atom [game-id :pieces :landing] landings)))))

;; (defn init! []
;;   ;; Invokes the main reagent hook function to kick things off at init time.   
;;   (reagent.dom/render [(game-fn)]
;;                       (.getElementById js/document "app")
;;                       (fn []
;;                         (events/post-event! {:event-id :events/events-after-init}))))

;;   ;; (events/subscribe-new-game! (fn [_]
;;   ;;                                (events/post-event! {:event-id}))
;;   ;;                      events/new-game 
;;   ;;                      :event-args 
;;   ;;                      [(utils/game-id-from-window)]))

;;   ;; (mount-root))

;; (defn post-game! []
;;   (async/take! (game-resource/create!)
;;                (fn [game-create-response]
;;                  (println "Requested new game.")
;;                  (println game-create-response))))
