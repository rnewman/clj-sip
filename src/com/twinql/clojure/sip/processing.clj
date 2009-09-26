(ns com.twinql.clojure.sip.processing
  (:refer-clojure)
  (:use com.twinql.clojure.sip.responses)
  (:import
     (java.lang Exception)
     (javax.servlet ServletException ServletInputStream)
     (javax.servlet.sip
       SipApplicationSession
       SipServlet
       SipServletMessage
       SipServletRequest
       SipServletResponse)))

;; SIP accessors.
(defn application-session-state [#^SipApplicationSession app-session]
  (.getAttribute app-session "state"))

(defn set-application-session-state! [#^SipApplicationSession app-session, state]
  (.setAttribute app-session "state" state))

(defn message-method [#^SipServletMessage message]
  (keyword (.toLowerCase (.getMethod message))))

;; SIP response code analysis.
(defn code->response [status]
  (*sip-code-map* status))

(defn response->code [response]
  (*sip-code-reverse-map* response))

(defn make-class-state-method-dispatcher
  "Dispatches on the servlet class, the current state, and the message method."
  [default-state]
  (fn [servlet app-session message]
    [(class servlet)
     (or 
       (application-session-state app-session)
       (do
         (set-application-session-state! app-session default-state)
         default-state))
     (message-method message)]))

(defn make-class-state-code-method-dispatcher
  [default-state]
  (fn [servlet app-session code message]
    [(class servlet)
     (or 
       (application-session-state app-session)
       (do
         (set-application-session-state! app-session default-state)
         default-state))
     (code->response code)
     (message-method message)]))
  
(defmacro defservlet
  "Your defservlet form must be in compiled code."
  ([name request-method-name response-method-name]
   `(defservlet ~name ~request-method-name ~response-method-name {}))
  ([name request-method-name response-method-name gen-class-options]
  `(do
     (gen-class
       :name ~name
       :extends SipServlet
       ;; Inline the keyword arguments.
       ~@(mapcat identity gen-class-options))
            
     (let [response-fn#
           (fn [this#, #^SipServletResponse res#]
             (~response-method-name this#
                (.getApplicationSession res#)
                (.getStatus res#)
                res#))]

       (defn ~'-doRequest [this#, #^SipServletRequest req#]
         (~request-method-name this#
            (.getApplicationSession req#)
            req#))

       (def ~'-doProvisionalResponse response-fn#)
       (def ~'-doSuccessResponse response-fn#)
       (def ~'-doRedirectResponse response-fn#)
       (def ~'-doErrorResponse response-fn#)))))
