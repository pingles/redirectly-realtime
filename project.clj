(defproject redirectly-realtime "0.1.0"
  :description "Provides realtime updates of clicks from RabbitMQ connection"
  :namespaces [
    redirectly-realtime.rabbit
    redirectly-realtime.esper
    redirectly-realtime.sample]
  :dependencies [
    [org.clojure/clojure "1.1.0"]
    [org.clojure/clojure-contrib "1.1.0"]
    [com.surftools/BeanstalkClient "1.0.0-SNAPSHOT"]
    [commons-logging/commons-logging "1.1.1"]
    [com.espertech/esper "3.3.0" :exclusions [commons-logging log4j]]
    [com.rabbitmq/amqp-client "1.7.2"]])