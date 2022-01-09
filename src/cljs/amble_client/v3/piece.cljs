(ns amble-client.v3.piece
  (:require [amble-client.v3.global-state :as gs]))

(defn piece [s [index & lookup-vals], add-on-attributes, mouse-event-fns]
  (let [d (nth (apply (partial gs/get s) lookup-vals)
               index)
        {:keys [x, y]} (:position d)
        {:keys [radius]} (:size d)
        ;; piece-key (concat lookup-vals [index])
        on-mouse-event-fn (fn [mouse-event-key]
                            (fn [e]
                              (apply (mouse-event-key mouse-event-fns)
                                     [(concat lookup-vals
                                             [index]) 
                                      e])))]
    [:circle (merge {:cx     x
                     :cy     y
                     :r      radius
                     :on-mouse-down (on-mouse-event-fn :on-mouse-down) 
                     :on-mouse-up (on-mouse-event-fn :on-mouse-up)} 
                                      
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
