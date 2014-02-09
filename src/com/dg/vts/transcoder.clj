(ns com.dg.vts.transcoder
  (:require [clj-json.core :as json]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io]
            [com.dg.vts.active-mq :as amq]
            [com.dg.vts.app-properties :as app-props])
  (:import [java.net URI]
           [java.util Date]
           [java.nio.file StandardCopyOption Files Path Paths]))

(defn copy-file-locally [remote-source-name local-dest-name]
  (let [remote-source-name (format "file://%s" (.replace remote-source-name "\\" "/"))
        local-dest-name (format "file:///%s" local-dest-name)
        source-path (Paths/get (URI/create remote-source-name))
        dest-path (Paths/get (URI/create local-dest-name))]
    (Files/copy source-path dest-path (into-array StandardCopyOption [StandardCopyOption/REPLACE_EXISTING]))))

(defn transcode-file [message]
  (when message
    (try
      (let [message-id (.getJMSMessageID message)
            params (json/parse-string (.getText message))
            remote-source (params "source")
            local-base-filename (str (java.util.UUID/randomUUID))
            local-source (format "%s/%s.%s" (app-props/props :dest-dir) local-base-filename
                                 (last (.split (last (.split remote-source "\\\\")) "\\.")))]
        (println (format "Transcoding message: %s" message-id))
        (copy-file-locally remote-source local-source)
        (let [retval ((apply shell/sh (.split (format(app-props/props :ffmpeg-command)
                                                     local-source (app-props/props :dest-dir) local-base-filename) " "))
                      :exit)]
          (println (format "%s transcoding message: %s"
                           (if (= retval 0) "Successfully finished" "Error while")
                           message-id))
          (io/delete-file local-source))
        true)
      (catch Throwable t
        false))))

(defn read-and-transcode-messages
  ([max-messages]
     (amq/with-consumer
       (read-and-transcode-messages max-messages consumer)))
  ([max-messages consumer]
     (doall
      (pmap transcode-file (repeatedly max-messages #(amq/read-from-q consumer))))))

(defn run-transcoder-agent []
  (let [concurrent-processes (app-props/props :concurrent-processes)]
    (println (format "Running transcoder agent with %d concurrent processes" concurrent-processes))
    (println (format "Transcoder agent started at : %s" (.toString (Date.))))
    (amq/with-consumer
      (let [total-messages-processed (atom 0)]
        (loop [ret-vals (read-and-transcode-messages concurrent-processes consumer)]
          (let [messages-processed (count (filter true? ret-vals))]
            (swap! total-messages-processed #(+ %1 messages-processed))          
            (when (> messages-processed 0)                                         
              (recur (read-and-transcode-messages concurrent-processes consumer)))))
        (println (format "Transcoder agent finished processing %d messages at : %s"
                         @total-messages-processed (.toString (Date.))))))))

(defn benchmark-transcoding [concurrent-processes max-n]
  (println (format "Processing %d messages : " (* concurrent-processes max-n)))
  (time
   (dotimes [n max-n]
     (read-and-transcode-messages concurrent-processes))))
