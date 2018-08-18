(ns kafka
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [me.raynes.fs :as fs])

  (:import [org.apache.curator.test TestingServer]
           [kafka.server KafkaConfig KafkaServerStartable]
           [org.apache.kafka.common.serialization StringSerializer StringDeserializer]
           [org.apache.kafka.clients.producer KafkaProducer ProducerRecord]
           [org.apache.kafka.clients.consumer KafkaConsumer]))


(defn start-zookeeper [port]
  (fs/delete-dir "/tmp/zk")
  (let [zk (TestingServer. port (io/file "/tmp/zk"))]
    (log/info "zk started")
    zk))


(defn start-kafka-server [zk-address]
  (let [config (KafkaConfig. {"zookeeper.connect"                zk-address
                              "listeners"                        "PLAINTEXT://127.0.0.1:9092"
                              "auto.create.topics.enable"        true
                              "offsets.topic.replication.factor" (short 1)
                              "offsets.topic.num.partitions"     (int 1)})
        kafka  (KafkaServerStartable. config)]
    (.startup kafka)
    (log/info "kafka started")
    kafka))


(defn handle-record [record]
  (println "Logic here for record" record))


(defn some-secret []
  (let [producer-props {"value.serializer"   StringSerializer
                        "key.serializer"     StringSerializer
                        "bootstrap.servers"  "127.0.0.1:9092"}
        consumer-props {"bootstrap.servers"  "127.0.0.1:9092"
                        "group.id"           "my-group"
                        "auto.offset.reset"  "earliest"
                        "enable.auto.commit" "true"
                        "key.deserializer"   StringDeserializer
                        "value.deserializer" StringDeserializer}


        kafka-producer (KafkaProducer. producer-props)
        consumer       (KafkaConsumer. consumer-props)]
    (.send kafka-producer (ProducerRecord. "temsan"  "search something"))
    (.subscribe consumer ["temsan"])
    (while true
      (let [records (.poll consumer 100)]
        (doseq [record records]
          (handle-record record))))))
