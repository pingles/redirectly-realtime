(ns redirectly-realtime.beanstalk_sample
  (:require
    [clojure.contrib.logging :as log]
    [redirectly-realtime.esper :as esper]
    [redirectly-realtime.beanstalk :as beanstalk])
  (:use    
    [clojure.contrib.json.read :only (read-json)]
    [clojure.contrib.json.write :only (json-str)]
    [clojure.contrib.command-line :only (with-command-line)])
  (:gen-class))

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
  (esper/send-event message "ClickEvent"))

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
  
(defn handler
  [message]
  (message-delivery message))

(defn run-client
  []
  (do
    (esper/attach-listener (esper/create-statement clicks-per-second-statement) (esper/create-listener log-count))
    (esper/attach-listener (esper/create-statement clicks-dropoff-statement) (esper/create-listener log-drop-off))
    (beanstalk/listen handler)))

(defn -main
  [& args]
  (run-client))