(ns amble.server.main
  (:require [re-frame.core :as re-frame]
            [martian.re-frame :as martian-reframe]
            [amble.server.server :as server]))  


(martian-reframe/init "http://localhost:9500/json/openapi.json" {:server-url "http://localhost:3000"})

(re-frame/reg-event-fx
  ::server/post-game
  (fn [{:keys [db]} [_ on-success, on-failure]]
    {:db (assoc db :posting-game? true)
     :dispatch [::martian-reframe/request
                :game-add          ;; Ensure this is the correct route id from your OpenAPI spec
                {}
                [on-success] ;; success callback event vector
                [on-failure]]})) ;; failure callback event vector
