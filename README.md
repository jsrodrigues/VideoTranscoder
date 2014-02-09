# video-transcoder

Wrote this program in Clojure for a Hackathon at DG. My intent was to explore the paralell processing features of Clojure. In the bargain, I also got to play with ffmpeg and Active MQ. On runnning the program (see instructions below), it starts a http listener (using Jetty) on a port configured in the properties.clj file. You can send a GET request to http://<server-name>:<port>/help to see the documentation on how to use the program.

The requirement was for the program to accept incominng requests to transcode a file using ffmpeg. The requests are placed on a queue for consumption by a transcoding agent. For the hackathon, we bufferend up 50 such requests and then had 2 agents running on 2 different machines consume the queue.

The same program can function as an agent and a master node. Master nodes have a configurable list of transcoder agents whom they notify to start transcoding. The notification in this case comes in the form of a HTTP request (again, use the in-built documentation @ http://<server-name>:<port>/help).


## Usage

You will need Active MQ for this to work. Setup ActiveMQ in your properties file

1. Create a uberjar with Leiningen (https://github.com/technomancy/leiningen)
   - lein uberjar (This produces a file called targets/vts.jar)
2. Run it like so:
   java -jar vts.jar <your path to property file>/properties.clj [There is a sample properties.clj file under resources]

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
