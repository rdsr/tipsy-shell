(ns tipsy-shell.channel
  (:use [tipsy-shell.http]))

(defn put-channel
  "Writes channel data (adds a segment) to an
import task as a base segment, creating a new
generation. Accepts the canonical channel
name and path to file which contains the data
to be uploaded. Also accepts an optional arg.
'content-type', if the content-type other than
'application/octet-stream' please specify.

Example
> (put-channel \"tipsy.ws.it.c\" \"/tmp/data.json\")
> (put-channel :tipsy.ws.it.c \"/tmp/data.json\") ;; same thing as above"
  [key path & [content-type]]
  (PUT (str "/ace/v1/channel/" (name key))
       (slurp path)
       {:content-type (or content-type "application/octet-stream")}))

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

Example
> (post-channel \"tipsy.ws.it.c\" \"/tmp/data.json\")
> (post-channel :tipsy.ws.it.c \"/tmp/data.json\") ;; same thing as above"
  [key path & [content-type]]
  (POST (str "/ace/v1/channel/" (name key))
        (slurp path)
        {:content-type (or content-type "application/octet-stream")}))
