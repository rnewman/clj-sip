(ns com.twinql.clojure.mime
  (:refer-clojure)
  (:import 
     (java.io ByteArrayInputStream)
     (javax.mail BodyPart
                 Multipart)
     (javax.mail.internet MimeMultipart
                          MimeBodyPart)))
 
(set! *warn-on-reflection* true)

(defn new-multipart [xs]
  (let [mp (new MimeMultipart)]
    (doseq [[x type] xs]
      (.addBodyPart mp (doto (new MimeBodyPart)
                         (.setContent x type))))
    mp))

(defn multipart-seq
  ([#^Multipart mp part-count i]
    (lazy-seq
      (when (< i part-count)
        (cons (.getBodyPart mp i)
              (multipart-seq mp part-count (inc i))))))
  ([#^Multipart mp]
   (multipart-seq mp (.getCount mp) 0)))

(defn multipart-part
  "Returns the first part of `mp` that has the provided `content-type`."
  [#^Multipart mp content-type]
  (some (fn [#^BodyPart part]
          (and (= content-type (.getContentType part))
               part))
        (multipart-seq mp)))

(defmulti content-byte-array (fn [x _] (class x)))

(defmethod content-byte-array String [#^String part max-length]
  (.getBytes part "UTF-8"))

(defmethod content-byte-array BodyPart [#^BodyPart part max-length]
  (content-byte-array (.getContent part) max-length))

(defmethod content-byte-array ByteArrayInputStream [#^ByteArrayInputStream part max-length]
  (let [byte (. Byte TYPE)
        buf (make-array byte max-length)
        read-count (.read part buf 0 max-length)]
    (if (= read-count max-length)
      buf
      (let [trimmed (make-array byte read-count)]
        (System/arraycopy buf 0 trimmed 0 read-count)
        trimmed))))

;; These return the first part of the appropriate type as a byte array.

(defmulti body-part (fn [x _ _] (class x)))

(defmethod body-part MimeMultipart [#^MimeMultipart content type length]
  (when-let [part (multipart-part content type)]
    (content-byte-array part length)))

(defn isup-body [content length]
  (body-part content "application/isup;version=ansi" length))

(defn sdp-body [content length]
  (body-part content "application/sdp" length))
