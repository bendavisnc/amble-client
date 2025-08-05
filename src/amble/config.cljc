(ns amble.config
  (:require-macros
   [amble.static-content.static-config :refer [defconfig]]))

(defconfig  CLIENT_URL :client-url)

(defconfig  SERVER_URL :server-url)

(defconfig  SERVER_HOST :server-host)

(defconfig  SERVER_PORT :server-port)
