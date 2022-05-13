(ns amble-client.v3.events)

(def events-new-game ::new-game)
(def events-after-init ::after-init)
(def subscribers-atomic {})

(defn get-subscribers [event-id]
  (or (get @subscribers-atomic event-id)
      (throw (str "No subscriber functions found for event-id, \""
                  event-id
                  "\"."))))

(defn on-event! [event-args]
  (dorun (for [subscriber-fn (get-subscribers ((:event-id event-args)))]
           (subscriber-fn event-args))))
      
(defn subscribe-to-event! [event-id, event-fn]
  (if (not (get @subscribers-atomic
                event-id))
    (swap! subscribers-atomic assoc event-id [event-fn])
    (swap! subscribers-atomic update-in [event-id] conj event-fn)))
  
(defn post-event! [event-args]
  (on-event! event-args))
  

(defn subscribe-new-game! [subscribe-function]
  (subscribe-to-event! events-new-game subscribe-function))

(defn subscribe-after-init! [subscribe-function]
  (subscribe-to-event! events-after-init subscribe-function))


(defn subscribe-new-mouse-move! [subscribe-function]
  (subscribe-to-event! ::new-mouse-move subscribe-function))
