(ns amble-client.v3.piece
  (:require [amble-client.v3.global-state :as gs]))

(defn piece [s [index & lookup-vals], add-on-attributes, mouse-event-fns]
  (let [d (nth (apply (partial gs/get s) lookup-vals)
               index)
        {:keys [x, y]} (:position d)
        {:keys [radius]} (:size d)
        ;; piece-key (concat lookup-vals [index])
        ;; _ (println "cooool")
        ;; _ (println piece-key)]
        _ (println add-on-attributes)]
    [:circle (merge {:cx     x
                     :cy     y
                     :r      radius
                     :key (str "wut" (:key add-on-attributes))
                     :data-x-key (:key add-on-attributes)
                    ;; :r      7
                    ;; :key piece-key
                     :on-mouse-down (fn [& args]
                                      (apply (:on-mouse-down mouse-event-fns)
                                             (concat [index] 
                                                     lookup-vals
                                                     args)))
                     :on-mouse-up (fn [& args]
                                      (apply (:on-mouse-up mouse-event-fns)
                                             (concat [index] 
                                                     lookup-vals
                                                     args)))}
                    add-on-attributes)]))


(defn landing-piece [s index, mouse-event-fns]
  (let [game-id (gs/get s :current)]
    (piece s
           [index, game-id, :pieces, :landing]
           {:key    (str "landing" "-" index)
            :id    (str "landing" "-" index)
            :class "landing"}
           mouse-event-fns)))


(defn player-piece [s player-id, index, mouse-event-fns]
  (let [game-id (gs/get s :current)
        player-id-name (name player-id)]
    (piece s
           [index, game-id, :pieces, :player, player-id]
           {:key (str "player" "-" player-id-name  "-" index)
            :id (str "player" "-" player-id-name "-" index)
            :class (str "player" " " player-id-name)}
           mouse-event-fns)))


    ;; [:circle {:cx     x
    ;;           :cy     y
    ;;           :r      radius
    ;;           :key    (str :player "-" player-id-name "-" index)
    ;;           :id    (str :player "-" player-id-name "-" index)
    ;;           :data-i index
    ;;           :class (str "player" " " player-id-name)
    ;;           :on-mouse-down (:on-mouse-down mouse-event-fns)
    ;;           :on-mouse-up (:on-mouse-up mouse-event-fns)}]))
