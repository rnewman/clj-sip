(ns print-hierarchy
  (:refer-clojure)
  (:use
     com.twinql.clojure.doc
     com.twinql.clojure.sip))

(hierarchy->dot @*sip-hierarchy*)
