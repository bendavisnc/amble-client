;; filepath: /home/ben/home/programming/amble/amble-client/test/amble/models/amble_test.cljs
(ns amble.models.amble-test
  (:require
   [amble.models.amble :as model]
   [amble.server.main :as server-default]
   [amble.server.server :as server]
   [cljs.test :refer-macros [testing is deftest]]
   [martian.re-frame :as martian-reframe]
   [re-frame.core :as re-frame]
   [day8.re-frame.test :as rf-test]))

(deftest initial-state-test
  (rf-test/run-test-async
  ;;  (martian-reframe/init "http://localhost:9500/json/openapi.json" {:server-url "http://localhost:3000"})
   (testing "initial state"
     (re-frame/dispatch [::model/initialize])
     (rf-test/wait-for [::model/on-player-success] 
                       (let [t (re-frame/subscribe [::model/amble])]
                         (is (= {:game {:TheThursdayGame {:game-id :TheThursdayGame, :player-one {:position [[0.425 0.7165] [0.475 0.7165] [0.525 0.7165] [0.575 0.7165] [0.45 0.7598] [0.5 0.7598] [0.55 0.7598] [0.475 0.8031] [0.525 0.8031] [0.5 0.8464]]}}}} 
                                @t)))))))
