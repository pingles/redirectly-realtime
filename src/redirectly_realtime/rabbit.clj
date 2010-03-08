(ns redirectly-realtime.rabbit
  (:import
    [com.rabbitmq.client ConnectionFactory Connection Channel ConnectionParameters QueueingConsumer])
  (:require
    [clojure.contrib.logging :as log]))

(def connection-params
  (doto (ConnectionParameters.)
    (.setUsername "guest")
    (.setPassword "guest")
    (.setVirtualHost "/")
    (.setRequestedHeartbeat 0)))
    
(def connection-factory
  (ConnectionFactory. connection-params))
  
(defn create-connection [host port]
  (.newConnection connection-factory host port))

(defn create-channel []
  (.createChannel (create-connection "localhost" 5672)))

;; initialises exchanges and queues
(defn setup-channel [channel exchange-name queue-name routing-key]
  (doto channel
    (.exchangeDeclare exchange-name "fanout")
    (.queueDeclare queue-name)
    (.queueBind queue-name exchange-name routing-key)))

(defn send-message [channel exchange-name routing-key message]
  (.basicPublish channel exchange-name routing-key nil (.getBytes message)))

(defn handle-delivery [body]
  (log/info (str "Received " (String. body))))

(defn listen-loop [channel queue-name]
  "Listens for messages in a loop. Can be run as follows: 
  (with-open [channel (create-channel)]
    (setup-channel channel \"clicks-exchange\" \"click-queue-<id>\" \"some-routing-key\")
    (listen-loop channel \"click-queue-<id>\"))"
  (let [consumer (QueueingConsumer. channel)]
    (do
      (.basicConsume channel queue-name true consumer)
      (while true
        (try
          (let [message (.nextDelivery consumer)] (handle-delivery (.getBody message)))
          (catch InterruptedException e (log/error e)))))))
