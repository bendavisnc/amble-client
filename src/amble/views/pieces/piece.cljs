(ns amble.views.pieces.piece
  (:require
   [clojure.string :as string]))



(def piece-size 0.023)

(defn piece [& {:keys [x, y, size, id, class, extra-opts]}]
  [:circle (merge {:cx x
                   :cy y
                   :r size
                   :key id
                   :id id
                   :class class}
                  extra-opts)])


;https://stackoverflow.com/questions/29261304/how-to-get-the-click-coordinates-relative-to-svg-element-holding-the-onclick-lis

;var pt = svg.createSVGPoint();  // Created once for document
;
;function alert_coords(evt) {
;    pt.x = evt.clientX;
;    pt.y = evt.clientY;
;
;    // The cursor point, translated into svg coordinates
;    var cursorpt =  pt.matrixTransform(svg.getScreenCTM().inverse());
;    console.log("(" + cursorpt.x + ", " + cursorpt.y + ")");
;}

; e.originalEvent.touches[0].clientX

(defn coord-conv []
  ;; (assert (= 0 @call-count))
    (fn [svg-element, e]
      (let [pt (.createSVGPoint svg-element)]
        (assert svg-element "No svg element provided to \"coord-conv\" util.")
        (assert (= (.getElementById js/document "board")
                   svg-element))
        (aset pt "x" (or (aget e "clientX")
                         (-> e
                             ;(aget "originalEvent")
                             (aget "touches")
                             (aget 0)
                             (aget "clientX"))))
        (aset pt "y" (or (aget e "clientY")
                         (-> e
                             ;(aget "originalEvent")
                             (aget "touches")
                             (aget 0)
                             (aget "clientY"))))
        (let [cursor-pt (.matrixTransform pt (.inverse (.getScreenCTM svg-element)))]
          [(aget cursor-pt "x")
           (aget cursor-pt "y")]))))
