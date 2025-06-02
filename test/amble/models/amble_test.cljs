;; filepath: /home/ben/home/programming/amble/amble-client/test/amble/models/amble_test.cljs
(ns amble.models.amble-test
  (:require
   [amble.models.amble :as model]
   [amble.server.main]
   [amble.specs.amble :as spec]
   [cljs.test :refer-macros [testing is deftest]]
   [clojure.spec.alpha :as s]
   [day8.re-frame.test :as rf-test]
   [re-frame.core :as re-frame]))

(deftest initial-state-test
  (rf-test/run-test-async
   (do
     (re-frame/dispatch [::model/initialize])
     (rf-test/wait-for [::model/on-player-six-success]
       (let [t (re-frame/subscribe [::model/amble])]
         (do
          (s/explain ::spec/component @t)
          (testing "initial state"
            (is (s/valid? ::spec/component @t)))))))))
          ;;  (is (= {:game {:TheFridayGame {:game-id :TheFridayGame, :player-four {:position [[0.5 0.1536] [0.475 0.1969] [0.525 0.1969] [0.45 0.2402] [0.5 0.2402] [0.55 0.2402] [0.425 0.2835] [0.475 0.2835] [0.525 0.2835] [0.575 0.2835]]}, :player-one {:position [[0.425 0.7165] [0.475 0.7165] [0.525 0.7165] [0.575 0.7165] [0.45 0.7598] [0.5 0.7598] [0.55 0.7598] [0.475 0.8031] [0.525 0.8031] [0.5 0.8464]]}, :player-two {:position [[0.275 0.5433] [0.25 0.5866] [0.3 0.5866] [0.225 0.6299] [0.275 0.6299] [0.325 0.6299] [0.2 0.6732] [0.25 0.6732] [0.3 0.6732] [0.35 0.6732]]}, :player-three {:position [[0.2 0.3268] [0.25 0.3268] [0.3 0.3268] [0.35 0.3268] [0.225 0.3701] [0.275 0.3701] [0.325 0.3701] [0.25 0.4134] [0.3 0.4134] [0.275 0.4567]]}, :player-five {:position [[0.65 0.3268] [0.7 0.3268] [0.75 0.3268] [0.8 0.3268] [0.675 0.3701] [0.725 0.3701] [0.775 0.3701] [0.7 0.4134] [0.75 0.4134] [0.725 0.4567]]}, :player-six {:position [[0.725 0.5433] [0.7 0.5866] [0.75 0.5866] [0.675 0.6299] [0.725 0.6299] [0.775 0.6299] [0.65 0.6732] [0.7 0.6732] [0.75 0.6732] [0.8 0.6732]]}}}}
          ;;         @t))))))))
            ;; (is (= 2
                  ;;  @t)))))))))

