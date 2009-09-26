(ns com.twinql.clojure.sip.responses
  (:refer-clojure)
  (:refer clojure.set)
  (:use com.twinql.clojure.hierarchy))

(def *sip-code-map*
  {100 :trying
   180 :ringing
   181 :call-is-being-forwarded
   182 :queued
   183 :session-progress

   200 :ok
   202 :accepted

   300 :multiple-choices
   301 :moved-permanently
   302 :moved-temporarily
   305 :use-proxy
   380 :alternative-service

   400 :bad-request
   401 :unauthorized
   402 :payment-required
   403 :forbidden
   404 :not-found
   405 :method-not-allowed
   406 :not-acceptable
   407 :proxy-authentication-required
   408 :request-timeout
   410 :gone
   412 :conditional-request-failed
   413 :request-entity-too-large
   414 :request-uri-too-long
   415 :unsupported-media-type
   416 :unsupported-uri-scheme
   417 :unknown-resource-priority
   420 :bad-extension
   421 :extension-required
   422 :session-interval-too-small
   423 :interval-too-brief
   424 :bad-location-information
   428 :use-identity-header
   429 :provide-referrer-identity
   433 :anonymity-disallowed
   436 :bad-identity-info
   437 :unsupported-certificate
   438 :invalid-identity-header
   480 :temporarily-unavailable
   481 :call-does-not-exist
   482 :loop-detected
   483 :too-many-hops
   484 :address-incomplete
   485 :ambiguous
   486 :busy-here
   487 :request-terminated
   488 :not-acceptable-here
   489 :bad-event
   491 :request-pending
   493 :undecipherable
   494 :security-agreement-required

   500 :server-internal-error
   501 :not-implemented
   502 :bad-gateway
   503 :service-unavailable
   504 :server-time-out
   505 :version-not-supported
   513 :message-too-large
   580 :precondition-failure

   600 :busy-everywhere
   603 :decline
   604 :does-not-exist-anywhere
   606 :not-acceptable
   })

(def *sip-code-reverse-map*
  (map-invert *sip-code-map*))

(def *sip-hierarchy*
  (ref 
    (deriving
      (make-hierarchy)
      
      ;; All messages are either requests or responses...
      (:sip-message
         :sip-request
         :sip-response)

      ;; Requests.
      (:sip-request
         :dialog-creating-request
         :subsequent-request
         :out-of-dialog-request)
     
      ;; These requests create a new dialog (or arrive within one).
      (:dialog-creating-request
         :invite
         :subscribe
         :notify
         :refer)
      
      ;; These requests can only arrive within an existing dialog.
      (:subsequent-request
         :acknowledgement
         :bye
         :cancel
         :update)
      
      ;; These requests don't create dialogs.
      (:out-of-dialog-request
         :info
         :options
         :register
         :publish)
      
      (:acknowledgement
         :ack
         :prack)
       
      ;; Connect requests to their source RFC.
      ;; Could easily make "through RFC 3265" etc. nodes for matching.
      (:rfc2976-requests
         :info)
      
      (:rfc3261-requests
         :ack
         :invite
         :options
         :bye
         :cancel
         :register)
      
      (:rfc3262-requests
         :prack)
      
      (:rfc3265-requests
         :subscribe
         :notify)
      
      (:rfc3311-requests
         :update)
      
      (:rfc3515-requests
         :refer)
      
      (:rfc3903-requests
         :publish)
      
      ;; Responses.
      (:sip-response
         :provisional-response
         :success-response
         :redirect-response
         :error-response)
      
      (:error-response
         :client-failure-response
         :server-failure-response
         :global-failure-response)

      (:post-trying-provisional-response
         :ringing
         :call-is-being-forwarded
         :queued
         :session-progress)
      
      (:provisional-response
         :trying
         :post-trying-provisional-response)
      
      (:success-response
         :ok
         :accepted)
      
      (:can-retry-response
         :should-retry-response
         :use-proxy)
         
      (:should-retry-response
         :moved-permanently
         :moved-temporarily)
         
      (:redirect-response
         :multiple-choices
         :can-retry-response
         :use-proxy
         :alternative-service)
      
      ;; Some responses in RFC 3261 SHOULD be retried.
      (:response-should-retry
         :response-should-retry-if-possible
         :response-should-retry-with-credentials)
      
      (:response-should-retry-with-credentials
         :unauthorized
         :proxy-authentication-required)
      
      (:response-should-retry-if-possible
         :request-entity-too-large
         :unsupported-media-type
         :unsupported-uri-scheme
         :bad-extension)
      
      (:client-failure-response
         :bad-request
         :unauthorized
         :payment-required
         :forbidden
         :not-found
         :method-not-allowed
         :not-acceptable
         :proxy-authentication-required
         :request-timeout
         :gone
         :conditional-request-failed
         :request-entity-too-large
         :request-uri-too-long
         :unsupported-media-type
         :unsupported-uri-scheme
         :unknown-resource-priority
         :bad-extension
         :extension-required
         :session-interval-too-small
         :interval-too-brief
         :bad-location-information
         :use-identity-header
         :provide-referrer-identity
         :anonymity-disallowed
         :bad-identity-info
         :unsupported-certificate
         :invalid-identity-header
         :temporarily-unavailable
         :call-does-not-exist
         :loop-detected
         :too-many-hops
         :address-incomplete
         :ambiguous
         :busy-here
         :request-terminated
         :not-acceptable-here
         :bad-event
         :request-pending
         :undecipherable
         :security-agreement-required)
      
      (:server-failure-response
         :server-internal-error
         :not-implemented
         :bad-gateway
         :service-unavailable
         :server-time-out
         :version-not-supported
         :message-too-large
         :precondition-failure)
      
      (:global-failure-response
         :busy-everywhere
         :decline
         :does-not-exist-anywhere
         :not-acceptable))))
