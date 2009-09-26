(ns com.twinql.clojure.doc
  (:refer-clojure))

(defn dotify [x]
  (.replace (name x) \- \_))

(defn hierarchy->dot
  "Print a given hierarchy as input suitable to `dot`.
  Optionally takes as input a map from elements to their parents,
  as produced by (:parents h)."
  ([h elems stream]
   (let [*out* stream]
     (hierarchy->dot h elems)))
  ([h elems]
   (println "digraph hierarchy {")
   (doseq [[child ps] elems]
     (doseq [parent ps]
       (println (str "  " (dotify child) " -> " (dotify parent) ";"))))
   (println "}"))
  ([h]
   (hierarchy->dot h (:parents h))))

(defn method-signatures
  "Return a sequence of the dispatch forms of `f`, excluding `:default`."
  [f]
  (filter #(not (= % :default)) (keys (methods f))))
  
;;
;; These are utility functions for checking the existence of a suitable
;; method. Note that we check two directions...
;; 

(defn request-method-exists? [h servlet-class state signature]
  (let [[se st me] signature]
    (and 
      ;; The method's class is a parent of ours...
      (isa? h servlet-class se)
      ;; The method's state is a parent of this one...
      (isa? h state st)

      ;; ... and the method is defined on any SIP request.
      ;; Note the intentional inversion.
      (isa? h me :sip-request))))

(defn response-method-exists? [h servlet-class state response-class signature]
  (let [[se st co me] signature]
    (and
      (isa? h servlet-class se)
      (isa? h state st)

      (isa? h co response-class)
      (isa? h me :sip-request))))

(defn servlet->doc
  "Point this function at a hierarchy. It produces dot output, and also checks
  that every state has suitable request and response handlers."
  ([h] (servlet->doc h :state))
  
  ([h root-state servlet-class req-f res-f]
   (let [state-map
         (filter (fn [[c ps]] (isa? h c root-state))
                 (:parents h))
         
         states (keys state-map)]
     
     ;; Error checking.
     (if (and req-f res-f)
       (let [req-methods (method-signatures req-f)
             res-methods (method-signatures res-f)]
         
         (println "States: " states)
        
         (let [entirely-unimplemented 
               (doall
                 (filter
                   (fn [state]
                     (not (or (some (partial request-method-exists? h servlet-class state)
                                    req-methods)
                              (some (partial response-method-exists? h servlet-class state
                                             :sip-response)
                                    res-methods))))
                   states))

               incomplete
               ;; Either there is a request handling method, or methods that handle
               ;; success, provisional, and error responses for that state.
               (doall
                 (filter (fn [state]
                           (not (or (some (partial request-method-exists? h servlet-class state)
                                          req-methods)
                                    
                                    ;; All three of success, provisional, error responses.
                                    (and
                                      (some (partial response-method-exists? h servlet-class
                                                     state
                                                     :success-response)
                                           res-methods)
                                      (some (partial response-method-exists? h servlet-class
                                                     state
                                                     :provisional-response)
                                           res-methods)
                                      (some (partial response-method-exists? h servlet-class
                                                     state
                                                     :error-response)
                                           res-methods)))))
                        states))]

           (when (seq entirely-unimplemented)
             (println "The following states are not matched by any handlers:")
             (println entirely-unimplemented))
           
           (when (seq incomplete)
             (println "The following states have missing handlers:")
             (println incomplete))))
       
       (println "No error checking being performed."))
     
     (newline)
     (hierarchy->dot h state-map))))
