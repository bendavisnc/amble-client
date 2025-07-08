(ns amble.views.move-count)

;; (defn move-count-text [move-index]
;;   (let [text (if (zero? move-index)
;;                "No moves yet"
;;                (str "Move #" move-index))]
;;     [:span.move-count-text text]))

(defn move-count-text [move-index]
  (let [text (if (zero? move-index)
               ""
               (str "#" move-index))]
    [:span.move-count-text text]))



(defn component [{:keys [move-index]}]
  [:div#move-count
    [move-count-text move-index]])
