(ns core
  (:require  [org.httpkit.server :as s]
             [cheshire.core :as json]
             [compojure.core :refer [defroutes POST GET ANY]]
             [kafka :as k]))


(def db (atom {:books {:lev [[1 "abababa"] [2 "galamaga"]]}}))


(defn add-filter [req]
  (let [body  (slurp (:body req))
        data  (json/parse-string body true)
        topic (get data :topic)
        q     (get data :q)]

    (swap! db (fn [atm]
                (assoc atm (keyword topic) (hash-map (keyword q) {}))))
    {:status 200
     :body   (format "filter %s added" q)}))


(defroutes app
  ;; TODO need search by id 
  (POST "/filter" req (add-filter req))
  (GET  "/filter" req
    {:status 200

     :body   (mapcat #(-> % second keys) (seq @db))})
  (GET "/:filter-name" [filter-name :as req]
    (let [db @db
          topics (keys db)
          values (filter (fn [[k v]]
                           (when (= (str k) filter-name)) v) topics)
          without-empty-data (remove nil? values)]
      {:status 200
       :body   (filter (fn [v] (second v))  without-empty-data)})))


(defn -main []
  (s/run-server app {:port 8083})
  ;(k/start-zookeeper 8084)
  ;(k/start-kafka-server "localhost:8084")
  ;(k/some-secret)
)

