(ns amble.static-content.static-config
  #?(:clj
     (:require
      [environ.core :refer [env]])))
 
#?(:clj (defmacro defconfig [sym k]
          (let [v (env k)]
            (if (nil? v)
              (throw (new Exception (format "Environment variable not set: `%s`" k)))
              `(def ~sym ~v)))))
