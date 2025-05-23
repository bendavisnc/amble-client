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
   (testing "initial state"
     (let [t (re-frame/subscribe [::model/amble])]
       (re-frame/dispatch [::model/initialize])
       [rf-test/wait-for [::model/on-player-six-success]
            (is (= 2
                  @t))]))))
