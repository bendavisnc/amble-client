(ns amble.models.amble-test
  (:require
   [cljs.test :refer-macros [testing is]]
   [day8.re-frame.test :refer [run-test-sync]]
   [devcards.core :refer-macros [deftest]]
   [amble.models.amble :as model]
   [amble.specs.amble :as spec]
   [re-frame.core :as re-frame]
   [clojure.spec.alpha :as s]))

(deftest initial-state-test
  (run-test-sync
   (let [t (re-frame/subscribe [::model/amble])]
     (testing "initial state"
       (re-frame/dispatch [:initialize])
       (is (= {:player-selected :player-one}
              @t))
       (is (s/valid? ::spec/component @t))))))
