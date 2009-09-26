(ns com.twinql.clojure.sip.util
  (:refer-clojure)
  (:use com.twinql.clojure.mime)
  (:import
     (java.lang Exception)
     (javax.servlet ServletException ServletInputStream)
     (javax.servlet.sip
       SipFactory
       SipServlet
       SipServletMessage
       SipServletRequest
       SipServletResponse
       SipURI)))

(defn servlet-factory [#^SipServlet servlet]
  (. (. servlet getServletContext)
     getAttribute "javax.servlet.sip.SipFactory"))

(defn sip-uri-maker
  [#^SipServlet servlet]
  (let [#^SipFactory factory (servlet-factory servlet)]
    (fn [user host]
      (.createSipURI factory user host))))

(defn success-response? [#^SipServletMessage x]
  (and (isa? x SipServletResponse)
       (let [#^SipServletResponse res x
             status (.getStatus res)]
         (and (>= status 200)
              (<  status 300)))))

(defn copy-sdp
  "Returns `to` for convenience."
  [#^SipServletMessage from
   #^SipServletMessage to]
  (try
    (let [len (.getContentLength from)]
      (when (pos? len)
        (let [#^bytes sdp (sdp-body from len)]
          (when sdp
            ;; Damn, need to define sdp as being a byte array, but can't!
            (.setContent to (new String sdp) "application/sdp")))))
    (catch Exception e nil))
  to)

(defn copy-content
  "Returns `to` for convenience."
  [#^SipServletMessage from
   #^SipServletMessage to]
  (when (pos? (.getContentLength from))
    (.setContent to
                 (.getContent from)
                 (.getContentType from))
    (let [#^String enc (.getCharacterEncoding from)]
      (when (and enc
                 (pos? (.length enc)))
        (.setCharacterEncoding to enc))))
  to)

(defn set-request-uri! [#^SipServletRequest req #^SipURI uri params]
  (.setRequestURI req (do
                        (doseq [[#^String param value] params]
                          (.setParameter uri param value))
                        uri))
  req)
