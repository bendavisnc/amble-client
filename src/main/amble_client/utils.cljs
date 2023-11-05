(ns amble-client.utils
  (:require
   [clojure.string :as string]))

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

(def call-count (atom 0))

(defn coord-conv [svg-element]
  (assert svg-element "No svg element provided to \"coord-conv\" util.")
  ;; (assert (= 0 @call-count))
  (swap! call-count inc)
  (let [pt (.createSVGPoint svg-element)]
    (fn [e]
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
