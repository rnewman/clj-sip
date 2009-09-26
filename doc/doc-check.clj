(ns doc-check
  (:refer-clojure)
  (:require
    ;; Echo.
    com.twinql.clojure.sip.example.echo
    com.twinql.clojure.sip.example.impl)
     
  (:use
     com.twinql.clojure.doc
     com.twinql.clojure.sip))

;; You can use these for debugging.
(comment
  (import 'java.lang.ClassLoader)
  (defn print-classpaths []
  (println (map #(.getFile %) (.getURLs (ClassLoader/getSystemClassLoader)))))
  (println (loaded-libs))
  (print-classpaths))

;; Do the error checking and graph printing for each application.

(newline)
(println "# Echo servlet:")
(com.twinql.clojure.doc/servlet->doc
  @com.twinql.clojure.sip.example.impl/*echo-hierarchy*
  :state
  com.twinql.clojure.sip.example.EchoServlet
  com.twinql.clojure.sip.example.impl/do-request
  com.twinql.clojure.sip.example.impl/do-response)
