# Introduction to video-transcoder

You will need Active MQ for this to work. Setup ActiveMQ in your properties file

1. Create a uberjar with Leiningen (https://github.com/technomancy/leiningen)
   - lein uberjar (This produces a file called targets/vts.jar)
2. Run it like so:
   java -jar vts.jar &lt;your path to property file&gt;/properties.clj [There is a sample properties.clj file under resources]
