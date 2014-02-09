# video-transcoder

Wrote this program in Clojure for a Hackathon at DG. My intent was to explore the paralell processing features of Clojure. In the bargain, I also got to play with ffmpeg and Active MQ. On runnning the program (see instructions below), it starts a http listener (using Jetty) on a port configured in the properties.clj file. You can send a GET request to http://&lt;server-name&gt;:&lt;port&gt;/help to see the documentation on how to use the program.

The requirement was for the program to accept incominng requests to transcode a file using ffmpeg. The requests are placed on a queue for consumption by a transcoding agent. For the hackathon, we bufferend up 50 such requests and then had 2 agents running on 2 different machines consume the queue.

The same program can function as an agent and a master node. Master nodes have a configurable list of transcoder agents whom they notify to start transcoding. The notification in this case comes in the form of a HTTP request (again, use the in-built documentation @ http://&lt;server-name&gt;:&lt;port&gt;/help).


## Usage

You will need Active MQ for this to work. Setup ActiveMQ in your properties file

1. Create a uberjar with Leiningen (https://github.com/technomancy/leiningen)
   - lein uberjar (This produces a file called targets/vts.jar)
2. Run it like so:
   java -jar vts.jar &lt;your path to property file&gt;/properties.clj [There is a sample properties.clj file under resources]

## License

Copyright © 2014

VideoTranscoder is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

VideoTranscoder is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
