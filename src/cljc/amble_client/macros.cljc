(ns amble-client.macros)

(defmacro el-env [& body]
  ; (let [env (clojure.edn/read-string (clojure.core/slurp (clojure.java.io/resource "public/json/environment/dev.json")))]
  (let [env "neat"]
    `(apply ~@body [~env])))
