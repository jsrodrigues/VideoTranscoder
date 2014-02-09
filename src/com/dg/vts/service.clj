(ns com.dg.vts.service
  (:use [ring.middleware.reload]
        [ring.middleware.params]
        [ring.middleware.json-params]
        [ring.middleware.stacktrace]
        [clojure.string :only [lower-case]])
  (:require [clj-http.client :as client]
            [clj-json.core :as json]
            [ring.adapter.jetty :as server]
            [com.dg.vts.app-properties :as app-props]
            [com.dg.vts.active-mq :as amq]
            [com.dg.vts.transcoder :as tcd])
  (:gen-class))

(defn validate-transcoding-message [transcoding-message]
  (println (set (map lower-case (keys transcoding-message))))
  (cond
   (nil? transcoding-message) [false "Empty message body"]
   (not (contains? (set (map lower-case (keys transcoding-message))) "source"))
   [false "No source tag in input"]
   :else [true]))

(defn process-transcoding-req [req]
  (let [resp {:status 200
              :headers {"Content-Type" "application/json"}
              :body (json/generate-string {:status "Queued transcoding request"})}
        [transcoding-message error-msg] (validate-transcoding-message (:json-params req))]
    (if transcoding-message
      (do
        (amq/write-to-q (json/generate-string transcoding-message))
        resp)
      (assoc resp :status 400 :body (json/generate-string {:error error-msg})))))

(defn get-documentation []
  {:note "Available Urls are"
   :urls [["/ping" "GET request"]
          ["/transcode" "POST - JSON payload of the form {\"source\" : \"<Source file location>\"}"]
          ["/stageMessages" "POST - JSON payload of the form {\"count\" : <number of messages>}"]
          ["/runTranscoderAgent" "POST - no payload"]
          ["/help" ""]]})

(defn stage-messages [req resp]
  (if-let [params (:json-params req)]
    (let [message-count (params "count" 0)]
      (.start (Thread. #(amq/batch-write-to-q 1 message-count)))
      (assoc resp :body (json/generate-string {"status" (format "%d messages are being written to the queue"
                                                                message-count)})))
    (assoc resp :status 400 :body (json/generate-string {"status" "No message count provided"}))))

(defn run-transcoder-agents [resp]
  (println "com.dg.tvs.service/run-transcoder-agents")
  (.start (Thread. #(time (tcd/run-transcoder-agent))))
  (when-let [agents (app-props/props :transcoder-agents)]
    (doseq [agent agents]
      (try
        (println (format "Running transcoder agent on %s" agent))
        (println (client/post (format "http://%s/runTranscoderAgents" agent)
                              {:content-type :json
                               :socket-timeout 1000
                               :conn-timeout 1000
                               :read-timeout 3000}))
        (catch Throwable t
          (println (format "Exception: %s" (.getMessage t)))))))
  (assoc resp :body (json/generate-string {:status "Launched transcoder agents"})))

(defn handler [req]
  (let [uri (req :uri)
        resp {:status 200
              :headers {"Content-Type" "application/json"}
              :body (json/generate-string {"Unhandled URI" uri})}]
    (cond
     (= uri "/ping") (assoc resp :body (json/generate-string
                                        {:ping "Video Transcoder service alive and well!!"}))
     (= uri "/transcode") (process-transcoding-req req)
     (= uri "/stageMessages") (stage-messages req resp)
     (= uri "/runTranscoderAgents") (run-transcoder-agents resp)
     (= uri "/help") (assoc resp :body (json/generate-string (get-documentation)))
     :else (assoc resp :body (json/generate-string (get-documentation))))))

(def app
  (-> handler
      wrap-reload
      wrap-params
      wrap-json-params
      wrap-stacktrace))

(defn -main [& args]
  (if (empty? args)
    (println "No property file supplied")
    (when (app-props/load-properties (first args))    
      (try
        (server/run-jetty app {:port (app-props/props :service-port)})
        (catch Throwable t
          (println (format "Exception %s occurred" (.getMessage t))))))))
