(ns redirectly-realtime.sample
  (:require
    [clojure.contrib.logging :as log]
    [redirectly-realtime.esper :as esper]
    [redirectly-realtime.rabbit :as rabbit])
  (:use    
    [clojure.contrib.json.read :only (read-json)]
    [clojure.contrib.json.write :only (json-str)]
    [clojure.contrib.command-line :only (with-command-line)])
  (:gen-class))

(def exchange "clicks-exchange")
(def routing-key "some-routing-key")
(def unique-id (str (java.util.UUID/randomUUID)))
(def queue-name (str "click-queue-" unique-id))

(defn log-count
  "Called once with each new event handled by Esper."
  [event]
  (log/info (format "%s sum=%s" (.get event "keyword") (.get event "cnt"))))

(defn log-drop-off
  "Called when a drop-off in count is detected."
  [event]
  (log/warn (format "%s %s received in last 10 seconds, average was %s" (.get event "cnt") (.get event "keyword") (.get event "avgCnt"))))
  
(defn message-delivery
  [message]
  (esper/send-event (read-json (String. message)) "ClickEvent"))

(def clicks-per-second-statement "
  insert into ClicksPerSecond
  select keyword, count(*) as cnt 
  from ClickEvent.win:time_batch(1 sec)
  group by keyword")

(def clicks-dropoff-statement "
  select keyword, avg(cnt) as avgCnt, cnt 
  from ClicksPerSecond.win:time(10 sec)
  group by keyword
  having cnt < avg(cnt)")

(defn client
  "Starts the rabbit listener for the exchange and attaches an
  Esper listener. Results are logged when raised."
  []
  (with-open [channel (rabbit/create-channel)]
    (esper/attach-listener (esper/create-statement clicks-per-second-statement) (esper/create-listener log-count))
    (esper/attach-listener (esper/create-statement clicks-dropoff-statement) (esper/create-listener log-drop-off))
    (rabbit/setup-channel channel exchange queue-name routing-key)
    (rabbit/listen-loop channel queue-name message-delivery)))

(defn click-message
  [keyword]
  (json-str {"keyword" keyword}))


(defn record-click
  "Posts simple JSON encoded message to exchange. Can be used
  to trigger results in client."
  [keyword]
  (with-open [channel (rabbit/create-channel)]
    (rabbit/send-message channel exchange routing-key (click-message keyword))))


(def *publisher-reps* 10000)

(defn publisher
  "Simulates sender, loads server with 10000 messages as quickly as possible."
  []
  (with-open [channel (rabbit/create-channel)]
    (time
      (dotimes [_ *publisher-reps*]
        (rabbit/send-message channel exchange routing-key (click-message "Sample Keyword Here"))))))

(def commands {"client" client, "publisher" publisher})
;; Starts the run-loop. Can be run fromm command-line as follows
;; java -server -cp redirectly-realtime-standalone.jar redirectly_realtime.sample
(defn -main
  [& args]
    (with-command-line args
      "Esper/RabbitMQ sample"
      [[run "Client or publisher." "client"]]
      (do
        (log/info (str "Starting " run " Esper/RabbitMQ sample. Logging with " log/*impl-name*))
        ((commands run)))))
