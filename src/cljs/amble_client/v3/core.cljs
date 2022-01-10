(ns amble-client.v3.core
  (:require [cljs.core.async :as async]
            [amble-client.resource.game :as game-resource]
            [amble-client.resource.board :as board-resource]
            [amble-client.resource.player :as player-resource]
            [amble-client.v3.board :as board]
            [amble-client.utils :as utils]
            [amble-client.v3.global-state :as gs]
            [goog.string :as gstring]
            [goog.string.format]
            [reagent.dom]
            [reagent.core :as reagent])
  (:require-macros [cljs.core.async :refer [go, go-loop]]))


(defn game-state-at-start [game-id]
  {:current game-id
   game-id {:pieces {:landing [{:position {:x 0.5, :y 0.5}
                                :size {:radius 0.023}}]
                     :player {:player-one [{:position {:x 0.5, :y 0.5}
                                            :size {:radius 0.023}}]}}}})

(def coord-conv-fn-atom (atom nil))

(def async-chan-mousedown (async/chan 1))
(def async-chan-mouseup (async/chan 1))
(def async-chan-mousemove (async/chan 1))
;; (def async-chan-mousemove (async/chan (async/sliding-buffer 1)
;;                                       (fn [e]
;;                                         (if-let [coord-conv
;;                                                  (deref coord-conv-fn-atom)]
;;                                          (do
;;                                            (println "handy")
;;                                            (coord-conv e))
;;                                          (do
;;                                            (throw (new js/Error "No coord conv fn available."))
;;                                            e)))))

                                           
(def async-chan-mousedrag (async/chan 1))

(def async-chan-ready (async/chan 1))

(go-loop []
  (let [lookup-vals (async/<! async-chan-mousedown)]
    (println "mouse pressed")
    (loop []
      (if (async/poll! async-chan-mouseup)
        (println "mouse released")
        (let [new-move-coord
              ((deref coord-conv-fn-atom)
               (async/<! async-chan-mousemove))]
          (do
            (async/>! async-chan-mousedrag [lookup-vals new-move-coord])
            ;; (async/>! async-chan-mousedrag [lookup-vals "wut"])
            (recur)))))
    (recur)))

(go-loop []
  (let [mouse-drag-event (async/<! async-chan-mousedrag)]
    (println "neatttt")
    (println (deref coord-conv-fn-atom))
    (println mouse-drag-event))
  (recur))

(go
  (async/<! async-chan-ready)
  (let [svg-target
        (.querySelector js/document 
                        "#app svg")]
    (assert (= "board" (.-id svg-target)))
    (reset! coord-conv-fn-atom (utils/coord-conv svg-target))
    (.addEventListener svg-target
                       "mousemove"
                       (fn [e]
                         (async/put! async-chan-mousemove 
                                     e)))))


(defmulti on-mouse-event (fn [lookup-vals, e]
                           (let [k
                                 (-> lookup-vals
                                     vec
                                     (subvec 1 3)
                                     (conj (keyword (.-type e))))]
                             (println k)
                             k)))

(defmethod on-mouse-event [:pieces :player :mousedown] 
  [lookup-vals, _]
  (async/put! async-chan-mousedown
              lookup-vals))

(defmethod on-mouse-event [:pieces :player :mouseup] 
  [lookup-vals, _]
  (async/put! async-chan-mouseup
              lookup-vals))


(defmethod on-mouse-event :default
  [lookup-vals, e]
  (println "No impl for mouse event")
  (println (clj->js [lookup-vals, e])))
                                              
(defn game-fn []
  (let [game-id (utils/game-id-from-window)
        ;; This is the game state that drives the whole client ui with react.
        reagent-atom-gs (reagent/atom (game-state-at-start game-id))]
    ;; Set up things like board coordinates asynchronously, once request resources are successfully made.
    (go (let [board-coords (async/<! (async/pipe (board-resource/get! game-id)
                                                 (async/chan 1
                                                             (map (fn [coordinates]
                                                                    (for [[x, y] coordinates]
                                                                     {:position {:x x, :y y}  
                                                                      :size {:radius 0.023}}))))))
              player-ids (async/<! (player-resource/get! game-id))
              player-coords (async/<! (async/into {}
                                                  (async/merge (for [player-id player-ids]
                                                                 (async/pipe (player-resource/get! game-id player-id)
                                                                             (async/chan 1
                                                                                         (map (fn [{:keys [player-id, coordinates]}]
                                                                                                [(keyword player-id) (for [[x, y] coordinates]
                                                                                                                      {:position {:x x, :y y}  
                                                                                                                       :size {:radius 0.023}})]))))))))]
          (println "Setting up board...")
          (gs/set! reagent-atom-gs [game-id :pieces :landing] board-coords) 
          (gs/set! reagent-atom-gs [game-id :pieces :player] player-coords))) 
    (fn []
      (let [game-state (deref reagent-atom-gs)]
        [:div {:id "amble"}
         [board/board game-state 
                      game-id
                      on-mouse-event]])))) 

(defn mount-root []
  (reagent.dom/render [(game-fn)] 
                      (.getElementById js/document "app")
                      (fn [] 
                        (async/put! async-chan-ready true))))
                       

(defn init! []
  (mount-root))

(defn post-game! []
  (async/take! (game-resource/create!)
               (fn [game-create-response]
                 (println "Requested new game.")
                 (println game-create-response))))
