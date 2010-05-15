(ns redirectly-realtime.beanstalk
  (:require
    [clojure.contrib.json.read :as jr]
    [clojure.contrib.json.write :as jw])
  (:import
    [com.surftools.BeanstalkClientImpl ClientImpl]))

(defn publisher
  "Publishes to tube"
  [tube]
  (let [client (ClientImpl. )]
    (.useTube client tube)
    client))

(defn consumer
  "Watches to tube"
  [tube]
  (doto (ClientImpl.) (.watch tube) (.ignore "default")))
    
(defn post-message
  [message client]
  (.put client 1 0 300 (.getBytes (jw/json-str message))))

(defn listen-to
  "Starts beanstalkd client and polls for messages. Forwards to Esper
  which can log on interesting stuff."
  [handler client]
  (loop [job (.reserve client nil)]
    (when (not (nil? job))
      (try
        (handler (jr/read-json (String. (.getData job))))
        (catch Exception e)
        (finally
          (.delete client (.getJobId job))))
      (recur (.reserve client nil)))))