(let [props {:dest-dir "C:/Users/jsrodrigues/VirtualMachines/shared/vts/workspace"
             :ffmpeg-command "C:/FFMpeg-64bit/bin/ffmpeg.exe -i %s -y -vcodec mpeg4 -acodec aac -strict -2 %s/%s.mp4"
             :activemq-url "tcp://research-dev.dgs.dgsystems.com:61616?jms.prefetchPolicy.queuePrefetch=1"
             :queue-name "vts"
             :service-port 9095
             :max-n 5
             :source-file "\\\\cifs.irvqafs1.qa.dgsystems.com\\mediadmz\\2014\\01\\23\\adhoc01227ph_072500\\adhoc01227ph_720p_20140123012236.mpg"
             :transcoder-agents ["research-dev.dgs.dgsystems.com:9095"]
             :concurrent-processes 4}]
  props)

