(ns redirectly-realtime.beanstalk
  (:require
    [clojure.contrib.json.read :as json])
  (:import
    [com.surftools.BeanstalkClientImpl ClientImpl]))

(defn listen
  "Starts beanstalkd client and polls for messages. Forwards to Esper
  which can log on interesting stuff."
  [handler]
  (let [client (ClientImpl. )]
    (loop [job (.reserve client nil)]
      (when (not (nil? job))
        (try
          (handler (json/read-json (String. (.getData job))))
          (catch Exception e)
          (finally
            (.delete client (.getJobId job))))
        (recur (.reserve client nil))))))