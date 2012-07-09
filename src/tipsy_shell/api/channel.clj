(ns tipsy-shell.api.channel
  (:use [tipsy-shell.http]
        [tipsy-shell.util :only (p-print)]))

(defn read-channels
  "Enumerate metadata about the channels.
> (read-channels :tipsy.ws.it)"
  [task-key]
  (p-print (GET (str "/ace/v1/channels/" (name task-key)))))

(defn read-channel
  "Read channel data, chunked. The datais
structured as a Chimp DataSet with the specified
number of items, and starting at the given offset
into the channel (as a whole).
> (read-channel :tipsy.ws.it.c)"
  [key & [snapshot start count]]
  (println (GET (str "/ace/v1/channel/" (name key))
                {:query-params {:snapshot snapshot
                                :start start
                                :count count}})))

(defn read-generations
  "Returns an enumeration of the generations
in the specified channel.
> (read-generations :tipsy.ws.it.c)"
  [channel-key]
  (p-print (GET (str "/ace/v1/generations/" (name channel-key)))))

(defn read-segments
  "Returns an enumeration of the segments in
the specified channel generation.
> (read-segments :tipsy.ws.it.c.20110805-141149-023)"
  [gen-key]
  (p-print (GET (str "/ace/v1/segments/" (name gen-key)))))

(defn read-segment
  "Read a segment of a raw channel. The segment
identifier is one of the values returned by the
/ace/v1/segments/{channel} call, which is to say
one of the keys of a written segment.
> (read-segment :tipsy.ws.it.c.20110805-141149-023.20110805-141149-023)"
  [seg-key & [format]]
  (println (GET (str "/ace/v1/segment/" (name seg-key))
                {:query-params {:format format}})))

(defn put-channel
  "Writes channel data (adds a segment) to anp
import task as a base segment, creating a new
generation. Accepts the canonical channel
name and path to file which contains the data
to be uploaded. Also accepts an optional arg.
'content-type', if the content-type other than
'application/octet-stream' please specify.
> (put-channel \"tipsy.ws.it.c\" \"/tmp/data.json\")
> (put-channel :tipsy.ws.it.c \"/tmp/data.json\") ;; same thing as above"
  [key path & [content-type]]
  (p-print
   (PUT (str "/ace/v1/channel/" (name key))
        (slurp path)
        {:content-type (or content-type "application/octet-stream")})))

(defn post-channel
  "Write channel data (adds a segment) to an
import task as a delta segment, adding to an
existing generation. Accepts the canonical
channel name and path to file which contains
the data to be uploaded. Also accepts an
optional arg. 'content-type', if the
content-type other than
'application/octet-stream' please specify.
Returns a response map.
> (post-channel \"tipsy.ws.it.c\" \"/tmp/data.json\")
> (post-channel :tipsy.ws.it.c \"/tmp/data.json\") ;; same thing as above"
  [key path & [content-type]]
  (p-print
   (POST (str "/ace/v1/channel/" (name key))
         (slurp path)
         {:content-type (or content-type "application/octet-stream")})))
