(defproject video-transcoder "0.1.0-SNAPSHOT"
  :description "Video Transcoder"
  :url "http://localhost:9095/"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-stacktrace "0.2.7"]
                 [ring "1.2.1"]
                 [clj-http "0.7.8"]
                 [clj-json "0.5.3"]
                 [ring-json-params "0.1.3"]
                 [org.apache.activemq/activemq-all "5.9.0"]]                 
  :uberjar-name "vts.jar"
  :profiles  {:uberjar {:aot :all
                        :main com.dg.vts.service}})
