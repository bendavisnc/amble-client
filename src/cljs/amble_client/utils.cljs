(ns amble-client.utils
  (:require
    [clojure.string :as string]
    [clojure.string :as s]))

(defn game-id-from-window
  "Returns the game id from the browser window."
  []
  (let [game-id
        (-> js/window
            (aget "location")
            (aget "pathname")
            (string/split "/")
            last)]
    (when (not game-id)
      (throw (new js/Error "No game id found in browser url.")))
    game-id))

(defn pieces-inferred-by-index [& {:keys [designatee-coords, piece-indexes]}]
  (map (fn [i]
         (or (get designatee-coords i)
             (throw (new js/Error (str "Invalid piece index, " i ".")))))
       piece-indexes))


(defn player-index [piece-elem]
  (let [index-listing ["one","two","three","four","five","six"]
        classname (first (s/split (.getAttribute piece-elem "class")
                                  #" "))
        match-value (s/replace classname "player-" "")]
    (assert (some #(= match-value
                      %)
                  index-listing)
            (str "Hacky element to player index value has proven not so good. \n Can't find match value, \"" match-value "\"."))
    (.indexOf index-listing match-value)))


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


(defn coord-conv [svg-element]
  (let [pt (.createSVGPoint svg-element)]
    (fn [e]
      (do
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
           (aget cursor-pt "y")])))))
