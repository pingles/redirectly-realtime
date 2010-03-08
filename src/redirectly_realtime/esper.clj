(ns redirectly-realtime.esper
  (:import
    [java.util Properties]
    [com.espertech.esper.client Configuration UpdateListener EPStatement EPServiceProviderManager])
  (:require
    [clojure.contrib.logging :as log]))
  

(def click-properties
  (doto (Properties.)
    (.put "keyword" "string")))

(def configuration
  (doto (Configuration.)
    (.addEventType "ClickEvent" click-properties)))

(defn log-event [event]
  (log/info (format "(last 30 seconds): keyword= %s, sum= %s" (.get event "keyword") (.get event "clicks"))))

(defn create-listener [fun]
  (proxy [UpdateListener] []
    (update [newEvents oldEvents]
      (apply fun newEvents))))

(def print-listener
  (create-listener log-event))

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
