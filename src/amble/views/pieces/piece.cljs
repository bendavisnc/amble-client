(ns amble.views.pieces.piece
  (:require
   [re-frame.core :as re-frame]))

(def piece-size 0.023)

(defn piece [& {:keys [x, y, size, id, class, extra-opts]}]
  [:circle (merge {:cx x
                   :cy y
                   :r size
                   :key id
                   :id id
                   :class class}
                  extra-opts)])

; https://stackoverflow.com/questions/29261304/how-to-get-the-click-coordinates-relative-to-svg-element-holding-the-onclick-lis

; var pt = svg.createSVGPoint();  // Created once for document
;
; function alert_coords(evt) {
;    pt.x = evt.clientX;
;    pt.y = evt.clientY;
;
;    // The cursor point, translated into svg coordinates
;    var cursorpt =  pt.matrixTransform(svg.getScreenCTM().inverse());
;    console.log("(" + cursorpt.x + ", " + cursorpt.y + ")");
; }

; e.originalEvent.touches[0].clientX

(defn coord-conv []
  (fn [svg-element, e]
    (assert svg-element "No svg element provided to \"coord-conv\" util.")
    (assert (= (.getElementById js/document "board")
               svg-element))
    (let [pt (.createSVGPoint svg-element)
          ptx (or (aget e "clientX")
                  (some-> e
                          (aget "touches")
                          (aget 0)
                          (aget "clientX"))
                  (do (println "No clientX found in event: " e)
                      0))
          pty (or (aget e "clientY")
                  (some-> e
                          (aget "touches")
                          (aget 0)
                          (aget "clientY"))
                  0)
          _ (assert (and ptx pty)
                    (str "No clientX or clientY found in event: " [ptx, pty]))]
      (aset pt "x" ptx)
      (aset pt "y" pty)
      (let [cursor-pt (.matrixTransform pt (.inverse (.getScreenCTM svg-element)))]
        [(aget cursor-pt "x")
         (aget cursor-pt "y")]))))

(def event-to-coord-fn (coord-conv))

(defn event-to-coord* [board-elem, e]
  (event-to-coord-fn board-elem e))

(defn e-to-event [e]
  ;; (.persist e)
  ;; (.preventDefault e)
  (let [event-to-coord (partial event-to-coord* (.getElementById js/document "board"))
        [x, y] (event-to-coord e)]
    {:x x
     :y y
     :event-type (.-type e)
     :client-x (.-clientX e)
     :client-y (.-clientY e)}))

(defn userfeedback-handler* [dispatch-map, e]
  (if-let [action (get dispatch-map (:event-type e))]
    ;; (println [::userfeedback-handler* action e])
    (re-frame/dispatch [action e])
    (println "No action found for event type: " (:event-type e))))
