(let [props {:dest-dir "<path to destination folder>"
             :ffmpeg-command "ffmpeg -i %s -y -vcodec mpeg4 -acodec aac -strict -2 %s/%s.mp4"
             :activemq-url "tcp://localhost:61616?jms.prefetchPolicy.queuePrefetch=1"
             :queue-name "vts"
             :service-port 9095
             :max-n 5
             :source-file "<source file location>/some-file.mpeg"
             :transcoder-agents ["localhost:9096"]
             :concurrent-processes 4}]
  props)
