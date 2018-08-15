(ns core
  (:require  [clojure.test :as t]
             [org.httpkit.server :as s]
             [compojure.core :refer [defroutes POST GET ANY]]
             [kafka :as k]))

(defroutes app
  (GET "/" [] "YO"))


(defn -main []
  (s/run-server app {:port 8083})
  (k/start-zookeeper 8084)
  (k/start-kafka-server "localhost:8084"))

