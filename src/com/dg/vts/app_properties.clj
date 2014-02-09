(ns com.dg.vts.app-properties)

(defn load-properties [property-file-location]
  (def props (load-file property-file-location))
  (def queue-name (props :queue-name))
  props)
