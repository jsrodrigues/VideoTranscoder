(ns com.dg.vts.active-mq
  (:require [clj-json.core :as json]
            [com.dg.vts.app-properties :as app-props])
  (:import [org.apache.activemq ActiveMQConnectionFactory]
           [javax.jms Connection
            DeliveryMode
            Destination
            Message
            MessageConsumer
            MessageProducer
            Session
            TextMessage]))

(defn get-connection []
  (let [con (.createConnection
             (ActiveMQConnectionFactory. (app-props/props :activemq-url)))]
    (.start con)
    con))

(defn get-session [con]
  (let [session (.createSession con false Session/AUTO_ACKNOWLEDGE)]
    session))

(defmacro with-producer [& body]
  `(with-open [con# (get-connection)
               ~'session (get-session con#)]
     (let [dest# (.createQueue ~'session (app-props/props :queue-name))]
       (with-open [~'producer (.createProducer ~'session dest#)]
         (.setDeliveryMode ~'producer DeliveryMode/PERSISTENT)
         ~@body))))

(defmacro with-consumer [& body]
  `(with-open [con# (get-connection)
               ~'session (get-session con#)]
     (let [dest# (.createQueue ~'session (app-props/props :queue-name))]
       (with-open [~'consumer (.createConsumer ~'session dest#)]
         ~@body))))

(defn write-to-q
  ([text-message]
     (with-producer
       (write-to-q text-message session producer)))
  ([text-message session producer]
     (let [message (.createTextMessage session text-message)]
       (.send producer message))))

(defn read-from-q 
  ([]
     (with-consumer
       (read-from-q consumer)))
  ([consumer]
     (.receive consumer 100)))

(defn batch-write-to-q [concurrent iterations]
  (let [source-file (app-props/props :source-file)]
    (time (doall 
           (pmap (fn [max-n]      
                   (with-producer
                     (dotimes [n max-n]
                       (write-to-q (json/generate-string {:source source-file}) session producer))))
                 (repeat concurrent iterations))))))

(defn batch-read-from-q [concurrent iterations]
  (let [counter (atom 0)]
    (time (do (doall 
               (pmap (fn [max-n]                     
                       (with-consumer
                         (dotimes [n max-n]
                           (when (read-from-q consumer)
                             (swap! counter inc)))))
                     (repeat concurrent iterations)))
              (println (format "Read %d messages" @counter))))))
