(in-ns 'com.twinql.clojure.sip.example.impl)

;; Utility macros for defining request/response handlers.
;; We can't specialize on EchoServlet yet, because it's only available
;; when the other file has been compiled and is available on the classpath.
;; Specialize on SipServlet instead.

;; Note that we have to do some fiddling with quote to get type annotations
;; to stick on the generated symbols.
(defmacro req-> [[state msg] & body]
  `(defmethod ~'do-request [SipServlet ~state ~msg]
     [~(quote #^SipServlet servlet)
      ~(quote #^SipApplicationSession app-session)
      ~(quote #^SipServletRequest req)]
     ~@body))

(defmacro res-> [[state res-kind msg] & body]
  `(defmethod ~'do-response [SipServlet ~state ~res-kind ~msg]
     [~(quote #^SipServlet servlet)
      ~(quote #^SipApplicationSession app-session)
      ~'code
      ~(quote #^SipServletResponse res)]
     ~@body))

(defmacro log [& forms]
  `(println (str "Echo: " ~@forms)))

;; Default handler.
(defmethod do-request :default
  [servlet app-session message]
  (log "Got some other message (" (message-method message) ") in state "
       (application-session-state app-session))
  (println (str message)))

;; 
;; Actual request/response handlers.
;; 
(req-> [:init :invite]
  (log "Got an INVITE in state "
       (application-session-state app-session)
       ". Sending response.")
  (set-application-session-state! app-session :propagated)
  (.send (.createResponse req 200)))

(req-> [:propagated :ack]
  (log "Got the ACK we expected. Sending BYE.")
  (.send (.createRequest (.getSession req) "BYE"))
  (set-application-session-state! app-session :bye-sent))
  
(res-> [:bye-sent :success-response :bye]
  (log "Got a success response to our BYE. Returning to init state.")
  (set-application-session-state! app-session :init))

;; Handle timeouts.
(res-> [:state :request-timeout :sip-request]
  (log "WARNING: Request timed out: " (.getRequest res)))

(res-> [:state :error-response :sip-request]
  (log "WARNING: Got an error response to our request: " (.getRequest res)))
