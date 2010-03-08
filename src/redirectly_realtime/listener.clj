(ns redirectly-realtime.listener
  (:import
    [java.util Properties]
    [com.espertech.esper.client Configuration UpdateListener EPStatement EPServiceProviderManager])
  (:require
    [clojure.contrib.logging :as log])
  (:gen-class))

(def click-properties
  (doto (Properties.)
    (.put "keyword" "string")))

(def configuration
  (doto (Configuration.)
    (.addEventType "ClickEvent" click-properties)))

(def print-listener
  (proxy [UpdateListener] []
    (update [newEvents oldEvents]
      (let [event (first newEvents)]
        (println (format "(last 30 seconds): keyword= %s, sum= %s" (.get event "keyword") (.get event "clicks")))))))

(def service
  (EPServiceProviderManager/getDefaultProvider configuration))

(defn create-statement [statement]
  (.createEPL (.getEPAdministrator service) statement))

(def count-statement
  (create-statement "select keyword, count(keyword) as clicks from ClickEvent.win:time(30 sec) group by keyword"))
  
(defn attach-listener [statement listener]
  (.addListener statement listener))

(defn send-event [event type]
  (.sendEvent (.getEPRuntime service) event type))

;; Starts the run-loop. Can be run fromm command-line as follows
;; java -cp redirectly-realtime-standalone.jar redirectly_realtime.listener
(defn -main
  []
  (log/info (str "Starting listener")))
