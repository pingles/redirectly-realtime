(defproject redirectly-realtime "0.1.0"
  :description "Provides realtime updates of clicks from RabbitMQ connection"
  :dependencies [
    [org.clojure/clojure "1.1.0"]
    [org.clojure/clojure-contrib "1.1.0"]
    [commons-logging/commons-logging "1.1.1"]
    [com.espertech/esper "3.3.0" :exclusions [log4j]]
    [com.rabbitmq/amqp-client "1.7.2"]])