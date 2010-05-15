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
  
(defn keyword-count-message
  "Creates outbound message with keyword and it's count"
  [event]
  (let [the-keyword (.get event "keyword") clicks-count (.get event "cnt")]
    {"eventType" "KeywordCount" "keyword" the-keyword "clicks" clicks-count}))
  
(defn log-count
  "Called once with each new event handled by Esper."
  [event]
  (beanstalk/post-message (keyword-count-message event) (beanstalk/publisher "interesting")))
  
(defn drop-off-message
  "Creates message with keyword and associated data"
  [event]
  (let [the-keyword (.get event "keyword") clicks-count (.get event "cnt") avg-count (.get event "avgCnt")]
    {"eventType" "CountDropOff" "keyword" the-keyword "clicks" clicks-count "avgClicks" avg-count}))

(defn log-drop-off
  "Called when a drop-off in count is detected."
  [event]
  (let [message (drop-off-message event)]
    (do
      (println (str "Drop-off detected: " message))
      (beanstalk/post-message message (beanstalk/publisher "interesting")))))

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
  
(defn click-event
  "Creates a ClickEvent hash from click arguments"
  [click]
  {"keyword" ((click "params") "q")})
  
(defn handler
  [message]
  (let [click-event (click-event message)]
    (do
      (println (str "ClickEvent received: " click-event))
      (esper/send-event click-event "ClickEvent"))))
    
(defn run-client
  []
  (do
    (esper/attach-listener (esper/create-statement clicks-per-second-statement) (esper/create-listener log-count))
    (esper/attach-listener (esper/create-statement clicks-dropoff-statement) (esper/create-listener log-drop-off))
    (beanstalk/listen-to handler (beanstalk/consumer "clicks"))))

(defn -main
  [nothing]
  (run-client))
