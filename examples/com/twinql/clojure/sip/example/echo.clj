(ns com.twinql.clojure.sip.example.impl
  (:require com.twinql.clojure.sip)
  (:refer-clojure)
  (:refer com.twinql.clojure.sip)
  (:import (javax.servlet.sip
             SipServlet
             SipServletResponse
             SipServletRequest)))

(set! *warn-on-reflection* true)

;; Random thought:
;; You can define your own hierarchies for tasks such as recognizing 
;; unsupported messages (always reject REGISTER, for example).

;; Put our states into a hierarchy, and use that for our methods.
;; :state is assumed as the root state.
(def *echo-hierarchy*
  (ref (deriving @*sip-hierarchy*
         (:state
            :baz            ; deliberately unused.
            :init
            :propagated
            :bye-sent))))

(defmulti do-request
          (make-class-state-method-dispatcher :init)
          :hierarchy *echo-hierarchy*)
(defmulti do-response
          (make-class-state-code-method-dispatcher :init)
          :hierarchy *echo-hierarchy*)

(ns com.twinql.clojure.sip.example.echo
  (:require com.twinql.clojure.sip)
  (:require com.twinql.clojure.sip.example.impl)
  
  (:refer-clojure)
  (:refer com.twinql.clojure.sip)
  (:refer com.twinql.clojure.sip.example.impl))

;; Define the servlet class. When this is compiled, a Java
;; class with the appropriate name will be defined. The 
;; implementation of its doRequest method will invoke the 
;; named Clojure function.
(defservlet com.twinql.clojure.sip.example/EchoServlet
            com.twinql.clojure.sip.example.impl/do-request
            com.twinql.clojure.sip.example.impl/do-response)
